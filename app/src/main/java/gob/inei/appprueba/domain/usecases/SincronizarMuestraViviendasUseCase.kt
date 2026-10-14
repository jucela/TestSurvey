package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.entities.EstadoArchivo
import gob.inei.appprueba.domain.entities.MuestraException
import gob.inei.appprueba.domain.entities.ResultadoArchivo
import gob.inei.appprueba.domain.entities.TipoError
import gob.inei.appprueba.domain.entities.TipoMuestra
import gob.inei.appprueba.domain.repositories.MuestraOnlineRepository
import javax.inject.Inject

/**
 * Sincroniza (descarga + carga) la muestra de viviendas de una combinación
 * ODEI/mes (RF-31/RF-32/RF-36/RF-37). Reutiliza exactamente el pipeline de carga
 * Offline; nunca lanza: devuelve el [ResultadoArchivo] para la lista por archivo.
 */
class SincronizarMuestraViviendasUseCase @Inject constructor(
    private val online: MuestraOnlineRepository,
    private val cargar: CargarMuestraUseCase
) {
    suspend operator fun invoke(odei: String, mes: String): ResultadoArchivo = try {
        val zip = online.obtenerViviendas(odei, mes)
        cargar("file://${zip.absolutePath}")
    } catch (e: MuestraException) {
        ResultadoArchivo(
            nombre = NOMBRE_FALLO,
            estado = EstadoArchivo.ERROR,
            tipo = TipoMuestra.VIVIENDAS,
            tipoError = e.tipo,
            detalle = e.detalle
        )
    } catch (e: Exception) {
        ResultadoArchivo(
            nombre = NOMBRE_FALLO,
            estado = EstadoArchivo.ERROR,
            tipo = TipoMuestra.VIVIENDAS,
            tipoError = TipoError.ERROR_DESCARGA,
            detalle = "No se pudo descargar la muestra"
        )
    }

    private companion object {
        const val NOMBRE_FALLO = "Muestra de viviendas"
    }
}
