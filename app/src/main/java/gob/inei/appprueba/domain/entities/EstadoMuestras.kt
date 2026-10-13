package gob.inei.appprueba.domain.entities

/** Estado de la muestra de personal para la tarjeta informativa (RF-27). */
data class EstadoMuestraPersonal(
    val cargado: Boolean,
    val fechaCargaMs: Long?
)

/** Meses cargados de una ODEI, ordenados y con dos dígitos (`01`..`12`) (RF-27). */
data class OdeiMeses(
    val odei: String,
    val nombreOdei: String,
    val meses: List<String>
)

/** Estado de la muestra de viviendas para la tarjeta informativa (RF-27). */
data class EstadoMuestraViviendas(
    val cargado: Boolean,
    val fechaCargaMs: Long?,
    val odeis: List<OdeiMeses>
)
