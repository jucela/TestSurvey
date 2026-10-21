package gob.inei.appprueba.domain.entities

/**
 * Modelo de dominio para el detalle de una vivienda en la pantalla Marco.
 * Combina datos de CAMPO_VIVIENDA_MARCO, CAMPO_VIVIENDA y CAMPO_HOGAR.
 */
data class ViviendaDetalle(
    /** Conglomerado (clave) */
    val conglome: String,
    /** Vivienda (número) */
    val vivienda: String,
    /** Número de vivienda (NROVIVIENDA) */
    val nroVivienda: String?,
    /** Tipo de vivienda: 1=seleccionada, 2=reemplazo (TIPOVIVIENDA) */
    val tipoVivienda: String?,
    /** Activo (1=sí, 0=no) */
    val activo: Int?,
    /** Número de reemplazo (NROREEMPLAZO de CAMPO_VIVIENDA) */
    val nroReemplazo: String?,
    /** ESREEMPLAZO: 1=reemplazo, 2/no reemplazo (para color fila) */
    val esReemplazo: String?,
    /** Hogar (HOGAR de CAMPO_HOGAR) */
    val hogar: String?,
    /** Resultado final (RESFIN mapeado a texto) */
    val resultado: String?,
    /** Dirección: prioridad CAMPO_VIVIENDA.NOMBREVIA → CAMPO_VIVIENDA_MARCO.NOMBREVIA */
    val direccion: String?,
    /** Puerta: prioridad CAMPO_VIVIENDA.PUERTA → CAMPO_VIVIENDA_MARCO.PUERTA */
    val puerta: String?,
    /** Interior: prioridad CAMPO_VIVIENDA.INTERIOR → CAMPO_VIVIENDA_MARCO.INTERIOR */
    val interior: String?,
    /** Piso: prioridad CAMPO_VIVIENDA.PISO → CAMPO_VIVIENDA_MARCO.PISO */
    val piso: String?,
    /** Manzana (Mz): prioridad CAMPO_VIVIENDA.MZ → CAMPO_VIVIENDA_MARCO.MZ */
    val manzana: String?,
    /** Lote: prioridad CAMPO_VIVIENDA.LOTE → CAMPO_VIVIENDA_MARCO.LOTE */
    val lote: String?,
    /** Indica si es reemplazo (ESREEMPLAZO == "1") para color fila */
    val esReemplazoFlag: Boolean
)