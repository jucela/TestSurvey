package gob.inei.appprueba.data.databases

import gob.inei.appprueba.data.databases.dao.OdeiMesRow
import gob.inei.appprueba.data.databases.entities.AlternativeEntity
import gob.inei.appprueba.data.databases.entities.AnswerEntity
import gob.inei.appprueba.data.databases.entities.FlowRuleEntity
import gob.inei.appprueba.data.databases.entities.QuestionEntity
import gob.inei.appprueba.data.databases.entities.MesEntity
import gob.inei.appprueba.data.databases.entities.OdeiEntity
import gob.inei.appprueba.data.databases.entities.PeriodoEntity
import gob.inei.appprueba.data.databases.projections.ConglomeradoMarcoRow
import gob.inei.appprueba.data.databases.projections.DetalleMarcoRow
import gob.inei.appprueba.data.databases.projections.MarcoFilaRow
import gob.inei.appprueba.domain.entities.DetalleViviendaMarco
import gob.inei.appprueba.domain.entities.FilaMarco
import gob.inei.appprueba.domain.entities.IndicadoresMarco
import gob.inei.appprueba.domain.entities.OpcionConglomerado
import gob.inei.appprueba.domain.entities.OpcionMuestra
import gob.inei.appprueba.domain.entities.PeriodoMarco
import gob.inei.appprueba.domain.entities.ReglasMarco
import gob.inei.appprueba.domain.entities.Alternative
import gob.inei.appprueba.domain.entities.Answer
import gob.inei.appprueba.domain.entities.FlowRule
import gob.inei.appprueba.domain.entities.OdeiMeses
import gob.inei.appprueba.domain.entities.Question

fun QuestionEntity.toDomain() = Question(
    id = id,
    anio = anio,
    capitulo = capitulo,
    campoTabla = campoTabla,
    padre = padre,
    orden = orden,
    sortOrder = sortOrder,
    tipo = tipo,
    componenteUi = componenteUi,
    numeracion = numeracion,
    titulo = titulo,
    subtitulo = subtitulo
)

fun Question.toEntity() = QuestionEntity(
    id = id,
    anio = anio,
    capitulo = capitulo,
    campoTabla = campoTabla,
    padre = padre,
    orden = orden,
    sortOrder = sortOrder,
    tipo = tipo,
    componenteUi = componenteUi,
    numeracion = numeracion,
    titulo = titulo,
    subtitulo = subtitulo
)

fun AlternativeEntity.toDomain() = Alternative(
    pregunta = pregunta,
    campoTabla = campoTabla,
    orden = orden,
    numeracion = numeracion,
    texto = texto,
    tipoOpcion = tipoOpcion,
    campoDependiente = campoDependiente
)

fun Alternative.toEntity(anio: Int = 0) = AlternativeEntity(
    anio = anio,
    pregunta = pregunta,
    campoTabla = campoTabla,
    orden = orden,
    numeracion = numeracion,
    texto = texto,
    tipoOpcion = tipoOpcion,
    campoDependiente = campoDependiente
)

fun FlowRuleEntity.toDomain() = FlowRule(
    id = id,
    anio = anio,
    origen = origen,
    condicion = condicion,
    accion = accion,
    destino = destino,
    instruccion = instruccion
)

fun FlowRule.toEntity() = FlowRuleEntity(
    id = id,
    anio = anio,
    origen = origen,
    condicion = condicion,
    accion = accion,
    destino = destino,
    instruccion = instruccion
)

fun AnswerEntity.toDomain() = Answer(preguntaId = preguntaId, valor = valor, textoLibre = textoLibre)

fun Answer.toEntity(actualizado: Long = System.currentTimeMillis()) = AnswerEntity(
    preguntaId = preguntaId,
    valor = valor,
    textoLibre = textoLibre,
    actualizado = actualizado
)

/**
 * Agrupa las combinaciones cargadas de viviendas (ODEI, nombre, mes) en la lista
 * de tarjetas informativas (RF-27): una entrada por ODEI con sus meses distintos
 * y en orden ascendente, mostrando solo meses con datos.
 */
fun List<OdeiMesRow>.toOdeiMeses(): List<OdeiMeses> = this
    .filter { it.mes != null }
    .groupBy { it.odei }
    .toSortedMap()
    .map { (odei, filas) ->
        OdeiMeses(
            odei = odei,
            nombreOdei = filas.firstNotNullOfOrNull { it.nombreOdei } ?: odei,
            meses = filas.mapNotNull { it.mes }.distinct().sorted()
        )
    }

fun MarcoFilaRow.toFilaMarco() = FilaMarco(
    conglome = conglome,
    vivienda = vivienda,
    nroVivienda = limpiar(nroVivienda),
    activo = activo?.let { it == 1 },
    hogar = limpiar(hogar),
    seleccion = ReglasMarco.seleccion(seleccion),
    nroReemplazo = limpiar(nroReemplazo),
    resultado = ReglasMarco.resultadoVisita(resfin),
    esReemplazo = ReglasMarco.esReemplazo(esReemplazo)
)

fun ConglomeradoMarcoRow.toOpcionConglomerado() = OpcionConglomerado(
    conglome = conglome,
    area = ReglasMarco.areaGeografica(estrato, tipoSeleccion),
    estrato = limpiar(estrato),
    tipoSeleccion = limpiar(tipoSeleccion)
)

fun DetalleMarcoRow.toDetalleViviendaMarco() = DetalleViviendaMarco(
    nombreVia = ReglasMarco.campo(viviendaNombreVia, marcoNombreVia),
    puerta = ReglasMarco.campo(viviendaPuerta, marcoPuerta),
    interior = ReglasMarco.campo(viviendaInterior, marcoInterior),
    piso = ReglasMarco.campo(viviendaPiso, marcoPiso),
    manzana = ReglasMarco.campo(viviendaMz, marcoMz),
    lote = ReglasMarco.campo(viviendaLote, marcoLote)
)

fun List<MarcoFilaRow>.toIndicadoresMarco(
    estrato: String?,
    tipoSeleccion: String?
): IndicadoresMarco {
    val viviendas = distinctBy { it.conglome to it.vivienda }
    return IndicadoresMarco(
        seleccionadas = viviendas.count { ReglasMarco.seleccion(it.seleccion) == "1" },
        reemplazos = viviendas.count { ReglasMarco.seleccion(it.seleccion) == "2" },
        estrato = limpiar(estrato),
        tipoSeleccion = limpiar(tipoSeleccion)
    )
}

fun OdeiEntity.toOpcionMuestraMarco() = OpcionMuestra(codOdei, nomOdei)

fun MesEntity.toOpcionMuestraMarco() = OpcionMuestra(codigo, nombre.orEmpty())

fun PeriodoEntity.toPeriodoMarco() = PeriodoMarco(codPer, nomPer.orEmpty())

/** Escapa los comodines de LIKE para que la busqueda sea literal. */
fun escaparLike(texto: String): String = texto
    .replace("\\", "\\\\")
    .replace("%", "\\%")
    .replace("_", "\\_")

private fun limpiar(valor: String?): String? = valor?.trim()?.takeIf { it.isNotEmpty() }