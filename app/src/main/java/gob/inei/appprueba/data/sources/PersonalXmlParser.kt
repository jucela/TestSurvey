package gob.inei.appprueba.data.sources

import gob.inei.appprueba.BuildConfig
import gob.inei.appprueba.domain.entities.MuestraException
import gob.inei.appprueba.domain.entities.Personal
import gob.inei.appprueba.domain.entities.TipoError
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * Parsea el XML de la muestra (iso-8859-1) y valida estructura y contenido (RF-06).
 * La raíz esperada es `ENAHO{año}` (RF-17). No incluye valores de campos en los
 * mensajes de error (RF-12).
 */
class PersonalXmlParser @Inject constructor() {

    /** Usa como año esperado el del aplicativo (`ANIO_APLICATIVO`, RF-17). */
    fun parse(input: InputStream): List<Personal> = parse(input, BuildConfig.ANIO_APLICATIVO)

    /**
     * @param anio año del aplicativo con el que debe coincidir la raíz `ENAHO{año}`.
     */
    fun parse(input: InputStream, anio: Int): List<Personal> {
        val root = try {
            val factory = DocumentBuilderFactory.newInstance().apply { endurecer() }
            val document = factory.newDocumentBuilder().parse(input)
            document.documentElement
        } catch (e: MuestraException) {
            throw e
        } catch (e: Exception) {
            throw MuestraException(TipoError.ESTRUCTURA_INVALIDA, "XML ilegible", e)
        } ?: throw MuestraException(TipoError.ESTRUCTURA_INVALIDA, "XML vacío")

        validarRaiz(root, anio)

        val contenedor = hijos(root).firstOrNull { nombre(it) == CONTENEDOR }
            ?: throw MuestraException(TipoError.ESTRUCTURA_INVALIDA, "Falta $CONTENEDOR")

        val cantidad = contenedor.getAttribute("cantidad").trim().toIntOrNull()
        if (cantidad == null || cantidad < 0) {
            throw MuestraException(TipoError.ESTRUCTURA_INVALIDA, "Atributo cantidad inválido")
        }

        val nodos = hijos(contenedor).filter { nombre(it) == REGISTRO }
        if (nodos.isEmpty()) {
            throw MuestraException(TipoError.ESTRUCTURA_INVALIDA, "La muestra no contiene registros")
        }
        if (nodos.size > MAX_REGISTROS) {
            throw MuestraException(TipoError.DATOS_INVALIDOS, "Se supera el máximo de registros")
        }
        if (nodos.size != cantidad) {
            throw MuestraException(
                TipoError.DATOS_INVALIDOS,
                "cantidad=$cantidad no coincide con ${nodos.size} registros"
            )
        }

        return nodos.mapIndexed { indice, nodo -> mapear(nodo, indice + 1) }
    }

    private fun mapear(nodo: Element, posicion: Int): Personal {
        val campos = HashMap<String, String?>(24)
        hijos(nodo).forEach { hijo ->
            campos[nombre(hijo)] = hijo.textContent?.trim().orEmpty().ifEmpty { null }
        }

        val dni = requerido(campos, "DNI", posicion)
        if (dni.length != 8) {
            throw MuestraException(TipoError.DATOS_INVALIDOS, "Registro $posicion: DNI de 8 caracteres")
        }
        val perfil = requerido(campos, "PERFIL", posicion)
        if (perfil.length != 2) {
            throw MuestraException(TipoError.DATOS_INVALIDOS, "Registro $posicion: PERFIL de 2 caracteres")
        }
        val nombre = requerido(campos, "NOMBRE", posicion)
        val apePaterno = requerido(campos, "APEPATERNO", posicion)
        val apeMaterno = requerido(campos, "APEMATERNO", posicion)
        val odei = requerido(campos, "ODEI", posicion)
        val activo = banderaObligatoria(campos, "ACTIVO", posicion)
        val cargo = requerido(campos, "CARGO", posicion)
        if (cargo.length != 2) {
            throw MuestraException(TipoError.DATOS_INVALIDOS, "Registro $posicion: CARGO de 2 caracteres")
        }

        val clave = campos["CLAVE"]
        if (clave != null && clave.length > 8) {
            throw MuestraException(TipoError.DATOS_INVALIDOS, "Registro $posicion: CLAVE excede 8 caracteres")
        }
        val usuario = campos["USUARIO"]
        if (usuario != null && usuario.length > 20) {
            throw MuestraException(TipoError.DATOS_INVALIDOS, "Registro $posicion: USUARIO excede 20 caracteres")
        }

        val fechaAlta = fecha(campos["FECHAALTA"], "FECHAALTA", posicion)
        val fechaBaja = fecha(campos["FECHABAJA"], "FECHABAJA", posicion)
        val fechaNac = fecha(campos["FECHANAC"], "FECHANAC", posicion)

        return Personal(
            dni = dni,
            perfil = perfil,
            nombre = nombre,
            apePaterno = apePaterno,
            apeMaterno = apeMaterno,
            odei = odei,
            dpto = campos["DPTO"],
            prov = campos["PROV"],
            dist = campos["DIST"],
            clave = clave,
            usuario = usuario,
            activo = activo,
            fechaAlta = fechaAlta,
            fechaBaja = fechaBaja,
            movil = banderaOpcional(campos["MOVIL"], "MOVIL", posicion),
            reentrevista = banderaOpcional(campos["REENTREVISTA"], "REENTREVISTA", posicion),
            pbasico = banderaOpcional(campos["PBASICO"], "PBASICO", posicion),
            pfinal = banderaOpcional(campos["PFINAL"], "PFINAL", posicion),
            cargo = cargo,
            fechaNac = fechaNac,
            telefono = campos["TELEFONO"],
            estadoCivil = campos["ESTADOCIVIL"],
            sexo = campos["SEXO"]
        )
    }

    private fun requerido(campos: Map<String, String?>, campo: String, posicion: Int): String =
        campos[campo]?.takeIf { it.isNotEmpty() }
            ?: throw MuestraException(TipoError.DATOS_INVALIDOS, "Registro $posicion: $campo vacío")

    private fun banderaObligatoria(campos: Map<String, String?>, campo: String, posicion: Int): Boolean {
        val valor = requerido(campos, campo, posicion)
        return parseBandera(valor)
            ?: throw MuestraException(TipoError.DATOS_INVALIDOS, "Registro $posicion: $campo debe ser 0 o 1")
    }

    private fun banderaOpcional(valor: String?, campo: String, posicion: Int): Boolean? {
        if (valor == null) return null
        return parseBandera(valor)
            ?: throw MuestraException(TipoError.DATOS_INVALIDOS, "Registro $posicion: $campo debe ser 0 o 1")
    }

    private fun parseBandera(valor: String): Boolean? = when (valor) {
        "1" -> true
        "0" -> false
        else -> null
    }

    private fun fecha(valor: String?, campo: String, posicion: Int): String? {
        if (valor == null) return null
        return normalizarFecha(valor)
            ?: throw MuestraException(TipoError.DATOS_INVALIDOS, "Registro $posicion: $campo con fecha inválida")
    }

    /** Normaliza a `dd/MM/aaaa` un serial de Excel, `dd/MM/aaaa` o `MM/dd/aaaa` (DP-8). */
    private fun normalizarFecha(valor: String): String? {
        val texto = valor.trim()
        if (texto.isEmpty()) return null

        SERIAL_FECHA.matchEntire(texto)?.let { match ->
            val serial = match.groupValues[1].toLongOrNull() ?: return null
            return fechaDesdeSerial(serial)
        }

        val partes = FORMATO_FECHA.matchEntire(texto)?.groupValues ?: return null
        val primero = partes[1].toIntOrNull() ?: return null
        val segundo = partes[2].toIntOrNull() ?: return null
        val anio = partes[3].toIntOrNull() ?: return null

        val (dia, mes) = when {
            primero > 12 && segundo > 12 -> return null
            primero > 12 -> primero to segundo
            segundo > 12 -> segundo to primero
            else -> primero to segundo // ambiguo: se prefiere dd/MM
        }
        return formatear(dia, mes, anio)
    }

    private fun fechaDesdeSerial(serial: Long): String? {
        if (serial < SERIAL_MIN || serial > SERIAL_MAX) return null
        val calendario = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US)
        calendario.clear()
        calendario.set(SERIAL_EPOCA_ANIO, Calendar.DECEMBER, SERIAL_EPOCA_DIA_MES)
        calendario.add(Calendar.DAY_OF_MONTH, serial.toInt())
        return formatear(
            calendario.get(Calendar.DAY_OF_MONTH),
            calendario.get(Calendar.MONTH) + 1,
            calendario.get(Calendar.YEAR)
        )
    }

    private fun formatear(dia: Int, mes: Int, anio: Int): String? {
        val texto = String.format(Locale.US, "%02d/%02d/%04d", dia, mes, anio)
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { isLenient = false }.parse(texto)
            texto
        } catch (_: Exception) {
            null
        }
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

    /**
     * La raíz debe ser `ENAHO{año}`. Si el año difiere del esperado, es una
     * discrepancia nombre/contenido (RF-17/DP-9); si no es una raíz de muestra,
     * es un error de estructura.
     */
    private fun validarRaiz(root: Element, anio: Int) {
        val raiz = nombre(root)
        if (raiz == "ENAHO$anio") return
        if (RAIZ_CON_ANIO.matches(raiz)) {
            throw MuestraException(
                TipoError.DISCREPANCIA_NOMBRE_CONTENIDO,
                "El año del contenido no coincide con el nombre"
            )
        }
        throw MuestraException(TipoError.ESTRUCTURA_INVALIDA, "Elemento raíz inesperado")
    }

    private fun hijos(element: Element): List<Element> {
        val children = element.childNodes
        val resultado = ArrayList<Element>(children.length)
        for (i in 0 until children.length) {
            val node = children.item(i)
            if (node.nodeType == Node.ELEMENT_NODE) resultado.add(node as Element)
        }
        return resultado
    }

    companion object {
        private const val CONTENEDOR = "PERSONALENTITY"
        private const val REGISTRO = "PERSONAL"
        private const val MAX_REGISTROS = 10_000
        private const val SERIAL_MIN = 1L
        private const val SERIAL_MAX = 2_958_465L // 9999-12-31
        private const val SERIAL_EPOCA_ANIO = 1899
        private const val SERIAL_EPOCA_DIA_MES = 30
        private val RAIZ_CON_ANIO = Regex("""^ENAHO\d{4}$""")
        private val FORMATO_FECHA = Regex("""(\d{1,2})/(\d{1,2})/(\d{4})""")
        private val SERIAL_FECHA = Regex("""(\d{1,7})""")
    }
}
