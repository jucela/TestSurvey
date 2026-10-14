package gob.inei.appprueba.data.sources

import gob.inei.appprueba.data.databases.entities.Cap100PanelEntity
import gob.inei.appprueba.data.databases.entities.ConglomeradoEntity
import gob.inei.appprueba.data.databases.entities.PersonaPanelEntity
import gob.inei.appprueba.data.databases.entities.ViviendaMarcoEntity
import gob.inei.appprueba.data.databases.entities.ViviendaReemplazoEntity
import gob.inei.appprueba.domain.entities.MuestraException
import gob.inei.appprueba.domain.entities.TipoError
import java.io.InputStream
import java.util.Locale
import javax.inject.Inject
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * Parsea y valida el `.cfg` (XML `iso-8859-1`) de la muestra de viviendas
 * (RF-19): raíz `ENAHO{año}`, cinco secciones con su `cantidad`, campos
 * obligatorios, tipos y relaciones entre secciones. Normaliza recortando
 * espacios y mapeando `P112A`→columna `P112a`; `ESTRATOSOC` se ignora (DC-5).
 *
 * No incluye valores de campos en los mensajes de error (RF-12).
 */
class ViviendaXmlParser @Inject constructor() {

    fun parse(input: InputStream): MuestraViviendas {
        val root = try {
            val factory = DocumentBuilderFactory.newInstance().apply { endurecer() }
            val document = factory.newDocumentBuilder().parse(input)
            document.documentElement
        } catch (e: MuestraException) {
            throw e
        } catch (e: Exception) {
            throw MuestraException(TipoError.ESTRUCTURA_VIVIENDAS_INVALIDA, "XML ilegible", e)
        } ?: throw MuestraException(TipoError.ESTRUCTURA_VIVIENDAS_INVALIDA, "XML vacío")

        val anioRaiz = validarRaiz(root)

        val seccionConglomerado = seccion(root, "CONGLOMERADOENTITY")
        val seccionVivienda = seccion(root, "VIVIENDAMARCOENTITY")
        val seccionReemplazo = seccion(root, "VIVIENDAREEMPLAZOENTITY")
        val seccionPersona = seccionOpcional(root, "PERSONAPANELENTITY")
        val seccionCap100 = seccionOpcional(root, "CAP100PANELENTITY")

        val conglomerados = registros(seccionConglomerado, "CAMPO_CONGLOMERADO", MAX_REGISTROS)
            .map { aConglomerado(it) }
        val viviendas = registros(seccionVivienda, "CAMPO_VIVIENDA_MARCO", MAX_REGISTROS)
            .map { aViviendaMarco(it) }
        val reemplazos = registros(seccionReemplazo, "CAMPO_VIVIENDA_REEMPLAZO", MAX_REGISTROS)
            .map { aReemplazo(it) }
        val personas = seccionPersona?.let { registros(it, "PERSONA_PANEL", MAX_REGISTROS) }
            ?.map { aPersona(it) } ?: emptyList()
        val cap100 = seccionCap100?.let { registros(it, "CAP100PANEL", MAX_REGISTROS) }
            ?.map { aCap100(it) } ?: emptyList()

        if (conglomerados.isEmpty()) {
            throw MuestraException(
                TipoError.ESTRUCTURA_VIVIENDAS_INVALIDA,
                "La sección CAMPO_CONGLOMERADO no contiene registros"
            )
        }

        val combinacion = combinacionUnica(conglomerados)
        validarRelaciones(conglomerados, viviendas, reemplazos, personas)

        return MuestraViviendas(
            anioRaiz = anioRaiz,
            anio = combinacion.anio,
            odei = combinacion.odei,
            mes = combinacion.mes,
            conglomerados = conglomerados,
            viviendas = viviendas,
            reemplazos = reemplazos,
            personas = personas,
            cap100 = cap100
        )
    }

    private fun validarRaiz(root: Element): String {
        val raiz = nombre(root)
        val match = RAIZ_CON_ANIO.matchEntire(raiz)
            ?: throw MuestraException(
                TipoError.ESTRUCTURA_VIVIENDAS_INVALIDA,
                "Elemento raíz inesperado"
            )
        return match.groupValues[1]
    }

    private fun seccion(root: Element, nombreSeccion: String): Element =
        hijos(root).firstOrNull { nombre(it) == nombreSeccion }
            ?: throw MuestraException(
                TipoError.ESTRUCTURA_VIVIENDAS_INVALIDA,
                "Falta la sección de viviendas"
            )

    private fun seccionOpcional(root: Element, nombreSeccion: String): Element? =
        hijos(root).firstOrNull { nombre(it) == nombreSeccion }

    private fun registros(seccion: Element, etiqueta: String, maximo: Int): List<Map<String, String?>> {
        val cantidad = seccion.getAttribute("cantidad").trim().toIntOrNull()
        if (cantidad == null || cantidad < 0) {
            throw MuestraException(
                TipoError.ESTRUCTURA_VIVIENDAS_INVALIDA,
                "Atributo cantidad inválido en $etiqueta"
            )
        }
        val nodos = hijos(seccion).filter { nombre(it) == etiqueta }
        if (nodos.size > maximo) {
            throw MuestraException(
                TipoError.ESTRUCTURA_VIVIENDAS_INVALIDA,
                "La sección $etiqueta supera el máximo de registros"
            )
        }
        if (nodos.size != cantidad) {
            throw MuestraException(
                TipoError.ESTRUCTURA_VIVIENDAS_INVALIDA,
                "cantidad de $etiqueta no coincide con los registros"
            )
        }
        return nodos.map { fila(it) }
    }

    private fun fila(element: Element): Map<String, String?> {
        val mapa = HashMap<String, String?>(48)
        hijos(element).forEach { hijo ->
            val clave = nombre(hijo)
            mapa[clave] = hijo.textContent?.trim().orEmpty().ifEmpty { null }
        }
        return mapa
    }

    private data class Combinacion(val anio: String, val odei: String, val mes: String)

    private fun combinacionUnica(conglomerados: List<ConglomeradoEntity>): Combinacion {
        val primero = conglomerados.first()
        val anio = primero.anho ?: throw falta("CAMPO_CONGLOMERADO", "ANHO")
        val odei = primero.odei ?: throw falta("CAMPO_CONGLOMERADO", "ODEI")
        val mes = primero.mes ?: throw falta("CAMPO_CONGLOMERADO", "MES")
        val uniforme = conglomerados.all { it.anho == anio && it.odei == odei && it.mes == mes }
        if (!uniforme) {
            throw MuestraException(
                TipoError.ESTRUCTURA_VIVIENDAS_INVALIDA,
                "CAMPO_CONGLOMERADO mezcla años, ODEIs o meses"
            )
        }
        return Combinacion(anio, odei, mes)
    }

    private fun validarRelaciones(
        conglomerados: List<ConglomeradoEntity>,
        viviendas: List<ViviendaMarcoEntity>,
        reemplazos: List<ViviendaReemplazoEntity>,
        personas: List<PersonaPanelEntity>
    ) {
        val conglomerosValidos = conglomerados.map { it.conglome }.toHashSet()
        if (viviendas.any { it.conglome !in conglomerosValidos }) {
            throw relacionInvalida("CAMPO_VIVIENDA_MARCO", "CAMPO_CONGLOMERADO")
        }
        if (reemplazos.any { it.conglome !in conglomerosValidos }) {
            throw relacionInvalida("CAMPO_VIVIENDA_REEMPLAZO", "CAMPO_CONGLOMERADO")
        }
        val viviendasValidas = viviendas.map { it.conglome to it.vivienda }.toHashSet()
        if (personas.any { (it.conglome to it.vivienda) !in viviendasValidas }) {
            throw relacionInvalida("PERSONA_PANEL", "CAMPO_VIVIENDA_MARCO")
        }
    }

    private fun relacionInvalida(hija: String, padre: String): MuestraException =
        MuestraException(
            TipoError.ESTRUCTURA_VIVIENDAS_INVALIDA,
            "$hija referencia registros inexistentes en $padre"
        )

    private fun aConglomerado(fila: Map<String, String?>): ConglomeradoEntity = ConglomeradoEntity(
        conglome = requerido(fila, "CONGLOME", "CAMPO_CONGLOMERADO"),
        anho = fila["ANHO"],
        mes = fila["MES"],
        periodo = fila["PERIODO"],
        estrato = fila["ESTRATO"],
        dpto = fila["DPTO"],
        prov = fila["PROV"],
        dist = fila["DIST"],
        zona = fila["ZONA"],
        dominio = fila["DOMINIO"],
        tipoSeleccion = fila["TIPOSELECCION"],
        odei = fila["ODEI"],
        aerIni = fila["AERINI"],
        aerFin = fila["AERFIN"],
        muestra = fila["MUESTRA"],
        sede = fila["SEDE"],
        prueba = booleano(fila["PRUEBA"]),
        covid19 = entero(fila["COVID19"]),
        usuarioDig = fila["USUARIODIG"],
        fechaDig = fila["FECHADIG"],
        usuarioMod = fila["USUARIOMOD"],
        fechaMod = fila["FECHAMOD"],
        usuCre = fila["USUCRE"],
        fecCre = fila["FECCRE"],
        usuReg = fila["USUREG"],
        fecReg = fila["FECREG"],
        fecEnv = fila["FECENV"]
    )

    private fun aViviendaMarco(fila: Map<String, String?>): ViviendaMarcoEntity = ViviendaMarcoEntity(
        conglome = requerido(fila, "CONGLOME", "CAMPO_VIVIENDA_MARCO"),
        vivienda = requerido(fila, "VIVIENDA", "CAMPO_VIVIENDA_MARCO"),
        manzana = fila["MANZANA"],
        nroVivienda = fila["NROVIVIENDA"],
        tipoVivienda = fila["TIPOVIVIENDA"],
        catVia = fila["CATVIA"],
        nombreVia = fila["NOMBREVIA"],
        puerta = fila["PUERTA"],
        interior = fila["INTERIOR"],
        piso = fila["PISO"],
        mz = fila["MZ"],
        lote = fila["LOTE"],
        km = fila["KM"],
        nroBlock = fila["NROBLOCK"],
        codCcpp = fila["CODCCPP"],
        nomCcpp = fila["NOMCCPP"],
        latOrigenGps = fila["LATORIGEN_GPS"],
        lonOrigenGps = fila["LONORIGEN_GPS"],
        usuarioDig = fila["USUARIODIG"],
        fechaDig = fila["FECHADIG"],
        usuarioMod = fila["USUARIOMOD"],
        fechaMod = fila["FECHAMOD"],
        activo = entero(fila["activo"] ?: fila["ACTIVO"]),
        usuCre = fila["USUCRE"],
        fecCre = fila["FECCRE"],
        usuReg = fila["USUREG"],
        fecReg = fila["FECREG"],
        fecEnv = fila["FECENV"]
    )

    private fun aReemplazo(fila: Map<String, String?>): ViviendaReemplazoEntity = ViviendaReemplazoEntity(
        conglome = requerido(fila, "CONGLOME", "CAMPO_VIVIENDA_REEMPLAZO"),
        vivienda = requerido(fila, "VIVIENDA", "CAMPO_VIVIENDA_REEMPLAZO"),
        mzn = fila["MZN"],
        tipVia = fila["TIPVIA"],
        nomVia = fila["NOMVIA"],
        ptaNro = fila["PTANRO"],
        nroBlock = fila["NROBLOCK"],
        nroPiso = fila["NROPISO"],
        intNro = fila["INTNRO"],
        intAlf = fila["INTALF"],
        nroMza = fila["NROMZA"],
        nroLote = fila["NROLOTE"],
        nroKm = fila["NROKM"],
        ocupada = fila["OCUPADA"],
        orden = fila["ORDEN"],
        ordenViv = fila["ORDENVIV"],
        seleccion = fila["SELECCION"],
        contiguaSel = fila["CONTIGUASEL"],
        reemplazo = fila["REEMPLAZO"],
        contiguaRee = fila["CONTIGUAREE"],
        seleccionAnt = fila["SELECCIONANT"],
        anhoAnt = fila["ANHOANT"],
        mesAnt = fila["MESANT"],
        dpto = fila["DPTO"],
        odei = fila["ODEI"],
        usoLocal = fila["USOLOCAL"],
        jefeHogar = fila["JEFEHOGAR"],
        reemplazoCampo = fila["REEMPLAZOCAMPO"],
        grupo = fila["GRUPO"],
        nomCcpp = fila["NOMCCPP"],
        aerIni = fila["AERINI"],
        aerFin = fila["AERFIN"],
        codCcpp = fila["CODCCPP"],
        latitud = fila["LATITUD"],
        longitud = fila["LONGITUD"],
        usuCre = fila["USUCRE"],
        fecCre = fila["FECCRE"],
        usuReg = fila["USUREG"],
        fecReg = fila["FECREG"],
        fecEnv = fila["FECENV"]
    )

    private fun aPersona(fila: Map<String, String?>): PersonaPanelEntity = PersonaPanelEntity(
        conglome = requerido(fila, "CONGLOME", "PERSONA_PANEL"),
        vivienda = requerido(fila, "VIVIENDA", "PERSONA_PANEL"),
        hogar = fila["HOGAR"],
        codPanel = requerido(fila, "CODPANEL", "PERSONA_PANEL"),
        p201 = fila["P201"],
        p202a = fila["P202A"],
        p202b = fila["P202B"],
        p202c = fila["P202C"],
        p203 = fila["P203"],
        p207 = fila["P207"],
        p208a = fila["P208A"],
        p301a = fila["P301A"],
        p301b = fila["P301B"],
        p401 = fila["P401"],
        p558c = fila["P558C"],
        p558cEspecifique = fila["P558C_ESPECIFIQUE"],
        usuCre = fila["USUCRE"],
        fecCre = fila["FECCRE"],
        usuReg = fila["USUREG"],
        fecReg = fila["FECREG"],
        fecEnv = fila["FECENV"],
        p204 = fila["P204"],
        p401h1 = fila["P401H1"],
        p401h2 = fila["P401H2"],
        p401h3 = fila["P401H3"],
        p401h4 = fila["P401H4"],
        p401h5 = fila["P401H5"],
        p401h6 = fila["P401H6"],
        p558a1 = fila["P558A1"],
        p558a2 = fila["P558A2"],
        p558a3 = fila["P558A3"],
        p558a4 = fila["P558A4"],
        p558a5 = fila["P558A5"],
        p558a6 = fila["P558A6"],
        p401h11 = fila["P401H11"],
        p401h12 = fila["P401H12"],
        p401h13 = fila["P401H13"],
        p401h14 = fila["P401H14"],
        p401h15 = fila["P401H15"],
        p401h16 = fila["P401H16"],
        p401h17 = fila["P401H17"],
        p558c1 = fila["P558C1"],
        p558c1Especifique = fila["P558C1_ESPECIFIQUE"]
    )

    private fun aCap100(fila: Map<String, String?>): Cap100PanelEntity = Cap100PanelEntity(
        anho = requerido(fila, "ANHO", "CAP100PANEL"),
        mes = requerido(fila, "MES", "CAP100PANEL"),
        conglome = requerido(fila, "CONGLOME", "CAP100PANEL"),
        vivienda = requerido(fila, "VIVIENDA", "CAP100PANEL"),
        hogar = requerido(fila, "HOGAR", "CAP100PANEL"),
        ubigeo = fila["UBIGEO"],
        dominio = fila["DOMINIO"],
        estrato = fila["ESTRATO"],
        p101 = fila["P101"],
        p102a = fila["P102A"],
        p103 = fila["P103"],
        p103a = fila["P103A"],
        p104 = fila["P104"],
        p104a = fila["P104A"],
        p105a = fila["P105A"],
        p110 = fila["P110"],
        p110a = fila["P110A"],
        p111 = fila["P111"],
        p112_1 = fila["P112_1"],
        p112_2 = fila["P112_2"],
        p112_3 = fila["P112_3"],
        p112_4 = fila["P112_4"],
        p112_5 = fila["P112_5"],
        p112_6 = fila["P112_6"],
        p112_7 = fila["P112_7"],
        p112a = fila["P112A"],
        usuCre = fila["USUCRE"],
        fecCre = fila["FECCRE"],
        usuReg = fila["USUREG"],
        fecReg = fila["FECREG"],
        fecEnv = fila["FECENV"],
        p106a = fila["P106A"],
        p113a = fila["P113A"],
        alert = fila["ALERT"]
    )

    private fun requerido(fila: Map<String, String?>, campo: String, seccion: String): String =
        fila[campo] ?: throw falta(seccion, campo)

    private fun falta(seccion: String, campo: String): MuestraException =
        MuestraException(
            TipoError.ESTRUCTURA_VIVIENDAS_INVALIDA,
            "$seccion: falta el campo obligatorio $campo"
        )

    private fun entero(valor: String?): Int? = valor?.toIntOrNull()

    private fun booleano(valor: String?): Boolean? = when (valor) {
        "1" -> true
        "0" -> false
        else -> null
    }

    private fun DocumentBuilderFactory.endurecer() {
        val features = listOf(
            "http://apache.org/xml/features/disallow-doctype-decl",
            "http://xml.org/sax/features/external-general-entities",
            "http://xml.org/sax/features/external-parameter-entities",
            "http://apache.org/xml/features/nonvalidating/load-external-dtd"
        )
        features.forEach { feature ->
            try {
                setFeature(feature, false)
            } catch (_: Exception) {
                // El parser de la plataforma puede no soportar la feature.
            }
        }
        runCatching { isXIncludeAware = false }
        runCatching { isExpandEntityReferences = false }
    }

    private fun nombre(element: Element): String = element.tagName.trim().uppercase(Locale.US)

    private fun hijos(element: Element): List<Element> {
        val children = element.childNodes
        val resultado = ArrayList<Element>(children.length)
        for (i in 0 until children.length) {
            val node = children.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) resultado.add(node as Element)
        }
        return resultado
    }

    private companion object {
        const val MAX_REGISTROS = 50_000
        val RAIZ_CON_ANIO = Regex("""^ENAHO(\d{4})$""")
    }
}
