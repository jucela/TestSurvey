package gob.inei.appprueba.domain.entities

/** Resultado de cargar una muestra (RF-03..RF-10). */
sealed interface CargaMuestraResult {
    data class Exito(val cargados: Int) : CargaMuestraResult
    data class Error(
        val tipo: TipoError,
        val detalle: String? = null
    ) : CargaMuestraResult
}

/** Motivos de error accionables, mapeados a mensajes en presentación (RF-11). */
enum class TipoError {
    /** El archivo no es un ZIP legible, está vacío o no es válido (corregible por el usuario). */
    ARCHIVO_INVALIDO,

    /** El ZIP no contiene el XML esperado. */
    XML_AUSENTE,

    /** El ZIP no pudo abrirse con la contraseña interna (requiere soporte). */
    CONTRASENA_INVALIDA,

    /** El método de cifrado del ZIP no está soportado (requiere soporte). */
    CIFRADO_NO_SOPORTADO,

    /** El XML no cumple la estructura esperada (RF-06). */
    ESTRUCTURA_INVALIDA,

    /** El XML contiene datos inválidos (campo, formato o cantidad). */
    DATOS_INVALIDOS,

    /** Falló la escritura local / la muestra no pudo persistirse. */
    ERROR_ESCRITURA,

    /** La contraseña interna no está configurada en el build. */
    CONFIGURACION,

    /** El nombre no corresponde a ningún formato de muestra conocido (RF-15). */
    TIPO_DESCONOCIDO,

    /** El nombre no cumple exactamente el formato de su tipo (RF-16). */
    NOMBRE_INVALIDO,

    /** El año del nombre no coincide con el año del aplicativo (RF-17). */
    ANIO_INVALIDO,

    /** El ODEI tiene formato inválido, está fuera de rango o no existe en la tabla (RF-18). */
    ODEI_INVALIDO,

    /** El mes no está en `01..12` o no es un mes calendario válido (RF-16). */
    MES_INVALIDO,

    /** El año/ODEI/mes del nombre no coincide con el contenido (RF-17, DP-9). */
    DISCREPANCIA_NOMBRE_CONTENIDO,

    /** El `.cfg` de viviendas no cumple la estructura esperada (RF-19). */
    ESTRUCTURA_VIVIENDAS_INVALIDA,

    /** No hay conexión de red o el servidor FTP no respondió (RF-32/RF-33). */
    SIN_CONEXION,

    /** El archivo/carpeta remoto no existe en el servidor FTP (RF-32/RF-33). */
    ARCHIVO_REMOTO_AUSENTE,

    /** Faltan host/usuario/contraseña FTP en la configuración del build (RF-34). */
    CONFIGURACION_FTP,

    /** Falló la descarga del ZIP remoto (red/escritura) (RF-35). */
    ERROR_DESCARGA
}

/** Excepción interna de la carga que transporta el [TipoError]. */
class MuestraException(
    val tipo: TipoError,
    val detalle: String? = null,
    cause: Throwable? = null
) : Exception(detalle ?: tipo.name, cause)
