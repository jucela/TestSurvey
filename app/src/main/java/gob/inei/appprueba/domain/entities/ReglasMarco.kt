package gob.inei.appprueba.domain.entities

/** Reglas puras de Marco, sin dependencias de Android (RF-07, RF-10, RF-14, RF-16, RF-18). */
object ReglasMarco {

    fun areaGeografica(estrato: String?, tipoSeleccion: String?): AreaGeografica {
        return when (tipoSeleccion?.trim()?.toIntOrNull()) {
            1, 2 -> AreaGeografica.URBANO_NO_PANEL
            3 -> when (estrato?.trim()?.toIntOrNull()) {
                6 -> AreaGeografica.SEMI_RURAL_PANEL
                1, 2, 3, 4, 5 -> AreaGeografica.URBANO_PANEL
                7 -> AreaGeografica.RURAL_PANEL
                else -> AreaGeografica.SIN_CLASIFICAR
            }
            4 -> AreaGeografica.SEMI_RURAL_NO_PANEL
            5 -> AreaGeografica.RURAL_NO_PANEL
            else -> AreaGeografica.SIN_CLASIFICAR
        }
    }

    fun resultadoVisita(resfin: String?): ResultadoVisita {
        return when (resfin?.trim()) {
            "1" -> ResultadoVisita.COMPLETA
            "2" -> ResultadoVisita.INCOMPLETA
            "3" -> ResultadoVisita.RECHAZO
            "4" -> ResultadoVisita.AUSENTE
            "5" -> ResultadoVisita.VIV_DESOCUPADA
            "6" -> ResultadoVisita.NO_SE_INICIO
            "7" -> ResultadoVisita.OTRO
            null, "" -> ResultadoVisita.SIN_VISITAR
            else -> ResultadoVisita.OTRO
        }
    }

    fun esReemplazo(valor: String?): Boolean = valor?.trim() == "1"

    /** Valor visible de Selección; el conteo solo considera 1 y 2. */
    fun seleccion(tipoVivienda: String?): String? =
        tipoVivienda?.trim()?.takeIf { it.isNotEmpty() }

    /** Prioriza CAMPO_VIVIENDA y usa CAMPO_VIVIENDA_MARCO como respaldo. */
    fun campo(principal: String?, respaldo: String?): String? {
        val valorPrincipal = principal?.trim()?.takeIf { it.isNotEmpty() }
        if (valorPrincipal != null) return valorPrincipal
        return respaldo?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun etiquetaOdei(codigo: String, nombre: String): String = "$codigo-$nombre"

    fun etiquetaMes(codigo: String, nombre: String): String = "$codigo-$nombre"

    fun etiquetaPeriodo(nombre: String): String = nombre
}
