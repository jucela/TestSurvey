package gob.inei.appprueba.data.databases

import gob.inei.appprueba.data.databases.entities.AlternativeEntity
import gob.inei.appprueba.data.databases.entities.AnswerEntity
import gob.inei.appprueba.data.databases.entities.FlowRuleEntity
import gob.inei.appprueba.data.databases.entities.QuestionEntity
import gob.inei.appprueba.domain.entities.Alternative
import gob.inei.appprueba.domain.entities.Answer
import gob.inei.appprueba.domain.entities.FlowRule
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
