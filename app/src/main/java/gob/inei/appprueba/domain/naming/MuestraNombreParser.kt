package gob.inei.appprueba.domain.naming

import gob.inei.appprueba.domain.entities.MuestraException
import gob.inei.appprueba.domain.entities.MuestraNombre
import gob.inei.appprueba.domain.entities.TipoError
import gob.inei.appprueba.domain.entities.TipoMuestra
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

/**
 * Identifica el tipo de muestra a partir del nombre del archivo y valida el
 * formato, el año, la fecha de generación (personal) y el ODEI/mes (viviendas)
 * sin tocar la base de datos (RF-15/RF-16/RF-17). La **existencia** del ODEI en
 * la tabla `ODEI` se comprueba en el repositorio (RF-18).
 *
 * Un nombre que no cumple ningún formato lanza [MuestraException] con el
 * [TipoError] correspondiente.
 */
class MuestraNombreParser @Inject constructor() {

    /**
     * @param nombre nombre del archivo (con o sin ruta); se usa el texto tal cual.
     * @param anioAplicativo año exigido a la muestra (`ANIO_APLICATIVO`).
     */
    fun parse(nombre: String, anioAplicativo: Int): MuestraNombre {
        val texto = nombre.trim()
        return when {
            texto.startsWith(PREFIJO_PERSONAL) -> parsePersonal(texto, anioAplicativo)
            texto.startsWith(PREFIJO_VIVIENDAS) -> parseViviendas(texto, anioAplicativo)
            else -> throw MuestraException(
                TipoError.TIPO_DESCONOCIDO,
                "El nombre no corresponde a una muestra de personal ni de viviendas"
            )
        }
    }

    private fun parsePersonal(nombre: String, anioAplicativo: Int): MuestraNombre {
        val match = FORMATO_PERSONAL.matchEntire(nombre)
            ?: throw MuestraException(
                TipoError.NOMBRE_INVALIDO,
                "El nombre de personal no cumple Personal_{AÑO}_Tablet-{AAAA-MM-DD}.zip"
            )
        val anio = match.groupValues[1].toInt()
        validarAnio(anio, anioAplicativo)
        val fecha = "${match.groupValues[2]}-${match.groupValues[3]}-${match.groupValues[4]}"
        if (!fechaCalendarioValida(fecha)) {
            throw MuestraException(TipoError.NOMBRE_INVALIDO, "Fecha de generación inválida")
        }
        return MuestraNombre(
            tipo = TipoMuestra.PERSONAL,
            anio = anio,
            fechaGeneracion = fecha
        )
    }

    private fun parseViviendas(nombre: String, anioAplicativo: Int): MuestraNombre {
        val match = FORMATO_VIVIENDAS.matchEntire(nombre)
            ?: throw MuestraException(
                TipoError.NOMBRE_INVALIDO,
                "El nombre de viviendas no cumple Enaho{AÑO}_Muestra_Odei_{ODEI}_Mes_{MES}.zip"
            )
        val anio = match.groupValues[1].toInt()
        validarAnio(anio, anioAplicativo)

        val odei = match.groupValues[2]
        if (odei.length != 2) {
            throw MuestraException(TipoError.ODEI_INVALIDO, "El ODEI debe tener dos dígitos")
        }
        val odeiNumero = odei.toInt()
        if (odeiNumero !in ODEI_MIN..ODEI_MAX) {
            throw MuestraException(TipoError.ODEI_INVALIDO, "El ODEI debe estar entre 01 y 26")
        }

        val mes = match.groupValues[3]
        if (mes.length != 2 || mes.toInt() !in MES_MIN..MES_MAX) {
            throw MuestraException(TipoError.MES_INVALIDO, "El mes debe estar entre 01 y 12")
        }

        return MuestraNombre(
            tipo = TipoMuestra.VIVIENDAS,
            anio = anio,
            odei = odei,
            mes = mes
        )
    }

    private fun validarAnio(anio: Int, anioAplicativo: Int) {
        if (anio != anioAplicativo) {
            throw MuestraException(
                TipoError.ANIO_INVALIDO,
                "El año de la muestra no corresponde al año del aplicativo"
            )
        }
    }

    private fun fechaCalendarioValida(fecha: String): Boolean {
        val formato = SimpleDateFormat(FORMATO_FECHA, Locale.US).apply { isLenient = false }
        val posicion = ParsePosition(0)
        formato.parse(fecha, posicion) ?: return false
        return posicion.index == fecha.length
    }

    private companion object {
        const val PREFIJO_PERSONAL = "Personal_"
        const val PREFIJO_VIVIENDAS = "Enaho"
        const val FORMATO_FECHA = "yyyy-MM-dd"
        const val ODEI_MIN = 1
        const val ODEI_MAX = 26
        const val MES_MIN = 1
        const val MES_MAX = 12

        val FORMATO_PERSONAL =
            Regex("""^Personal_(\d{4})_Tablet-(\d{4})-(\d{2})-(\d{2})\.zip$""")
        val FORMATO_VIVIENDAS =
            Regex("""^Enaho(\d{4})_Muestra_Odei_(\d{1,2})_Mes_(\d{1,2})\.zip$""")
    }
}
