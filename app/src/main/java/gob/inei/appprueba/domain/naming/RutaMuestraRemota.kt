package gob.inei.appprueba.domain.naming

/**
 * Construye las rutas remotas de la muestra publicada en el servidor FTP
 * (RF-32/RF-33). Es lógica pura (sin Android) para poder probarse en JVM.
 *
 * Convención: `DescargasEnaho{AÑO}/Muestra/{MM_Mes}/{archivo}.zip` para
 * viviendas y `DescargasEnaho{AÑO}/Muestra/Personal` para personal. En el nombre
 * de archivo el ODEI y el mes se expresan con dos dígitos (`Odei_01`, `Mes_01`),
 * tal como los publica el servidor (p. ej. `Enaho2026_Muestra_Odei_01_Mes_10.zip`).
 */
object RutaMuestraRemota {

    private val ABREVIATURAS = listOf(
        "Ene", "Feb", "Mar", "Abr", "May", "Jun",
        "Jul", "Ago", "Set", "Oct", "Nov", "Dic"
    )

    /** `DescargasEnaho{AÑO}/Muestra`. */
    fun directorioBase(anio: Int): String = "DescargasEnaho$anio/Muestra"

    /** `DescargasEnaho{AÑO}/Muestra/Personal`. */
    fun directorioPersonal(anio: Int): String = "${directorioBase(anio)}/Personal"

    /** Carpeta mensual `MM_Mes` (p. ej. `01_Ene`); usa el código `01..12`. */
    fun carpetaMes(mes: String): String {
        val numero = numeroEnRango(mes, 1, 12, "mes")
        return "%02d_%s".format(numero, ABREVIATURAS[numero - 1])
    }

    /** Nombre de archivo `Enaho{AÑO}_Muestra_Odei_{ODEI}_Mes_{MES}.zip`. */
    fun nombreArchivoViviendas(anio: Int, odei: String, mes: String): String {
        val odeiNumero = numeroEnRango(odei, 1, 26, "ODEI")
        val mesNumero = numeroEnRango(mes, 1, 12, "mes")
        return "Enaho${anio}_Muestra_Odei_%02d_Mes_%02d.zip".format(odeiNumero, mesNumero)
    }

    /** Ruta remota completa de la muestra de viviendas (RF-32). */
    fun rutaViviendas(anio: Int, odei: String, mes: String): String =
        "${directorioBase(anio)}/${carpetaMes(mes)}/${nombreArchivoViviendas(anio, odei, mes)}"

    /** Ruta remota completa de un ZIP de personal (RF-33). */
    fun rutaPersonal(anio: Int, nombreArchivo: String): String =
        "${directorioPersonal(anio)}/$nombreArchivo"

    private fun numeroEnRango(valor: String, min: Int, max: Int, etiqueta: String): Int {
        val numero = valor.toIntOrNull()
            ?: throw IllegalArgumentException("$etiqueta inválido: $valor")
        require(numero in min..max) { "$etiqueta fuera de rango: $valor" }
        return numero
    }
}
