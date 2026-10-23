package gob.inei.appprueba.data.databases.projections

/** Proyeccion de una fila de la tabla Marco (una por hogar). */
data class MarcoFilaRow(
    val conglome: String,
    val vivienda: String,
    val nroVivienda: String?,
    val activo: Int?,
    val hogar: String?,
    val seleccion: String?,
    val nroReemplazo: String?,
    val resfin: String?,
    val esReemplazo: String?
)

/** Proyeccion de un conglomerado con los datos del area geografica. */
data class ConglomeradoMarcoRow(
    val conglome: String,
    val estrato: String?,
    val tipoSeleccion: String?
)

/** Proyeccion de los campos de direccion de ambas tablas de vivienda. */
data class DetalleMarcoRow(
    val marcoNombreVia: String?,
    val marcoPuerta: String?,
    val marcoInterior: String?,
    val marcoPiso: String?,
    val marcoMz: String?,
    val marcoLote: String?,
    val viviendaNombreVia: String?,
    val viviendaPuerta: String?,
    val viviendaInterior: String?,
    val viviendaPiso: String?,
    val viviendaMz: String?,
    val viviendaLote: String?
)