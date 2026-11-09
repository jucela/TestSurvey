package gob.inei.appprueba.domain.entities

/** Detalle de direccion de una vivienda (RF-16/RF-17). */
data class DetalleViviendaMarco(
    val nombreVia: String?,
    val puerta: String?,
    val interior: String?,
    val piso: String?,
    val manzana: String?,
    val lote: String?,
    val conglome: String? = null,
    val nroVivienda: String? = null,
    val vivienda: String? = null
)
