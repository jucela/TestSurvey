package gob.inei.appprueba.domain.entities

/** Detalle de dirección de una vivienda (RF-16). */
data class DetalleViviendaMarco(
    val nombreVia: String?,
    val puerta: String?,
    val interior: String?,
    val piso: String?,
    val manzana: String?,
    val lote: String?
)
