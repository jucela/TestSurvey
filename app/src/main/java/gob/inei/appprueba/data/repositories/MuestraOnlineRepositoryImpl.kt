package gob.inei.appprueba.data.repositories

import gob.inei.appprueba.data.sources.AnioAplicativoProvider
import gob.inei.appprueba.data.sources.DirectorioConfigProvider
import gob.inei.appprueba.data.sources.FtpCliente
import gob.inei.appprueba.domain.entities.MuestraDescargada
import gob.inei.appprueba.domain.entities.MuestraException
import gob.inei.appprueba.domain.entities.TipoError
import gob.inei.appprueba.domain.entities.TipoMuestra
import gob.inei.appprueba.domain.naming.MuestraNombreParser
import gob.inei.appprueba.domain.naming.RutaMuestraRemota
import gob.inei.appprueba.domain.repositories.MuestraOnlineRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementación del acceso remoto a muestras (RF-32/RF-33/RF-35): resuelve la
 * ruta FTP, descarga el ZIP al directorio `config` y devuelve el archivo local.
 * Reutiliza [MuestraNombreParser] para elegir la muestra de personal más
 * reciente y no duplica la lógica de nombres.
 */
@Singleton
class MuestraOnlineRepositoryImpl @Inject constructor(
    private val ftp: FtpCliente,
    private val anioProvider: AnioAplicativoProvider,
    private val directorioConfig: DirectorioConfigProvider,
    private val parser: MuestraNombreParser
) : MuestraOnlineRepository {

    override suspend fun obtenerViviendas(odei: String, mes: String): File =
        withContext(Dispatchers.IO) {
            val anio = anioProvider.obtener()
            val nombre = RutaMuestraRemota.nombreArchivoViviendas(anio, odei, mes)
            val destino = File(directorioConfig.obtener(), nombre)
            ftp.descargar(RutaMuestraRemota.rutaViviendas(anio, odei, mes), destino)
            destino
        }

    override suspend fun obtenerPersonalMasReciente(): MuestraDescargada =
        withContext(Dispatchers.IO) {
            val anio = anioProvider.obtener()
            val nombres = ftp.listarNombres(RutaMuestraRemota.directorioPersonal(anio))
            val masReciente = seleccionarMasReciente(nombres, anio)
                ?: throw MuestraException(
                    TipoError.ARCHIVO_REMOTO_AUSENTE,
                    "El servidor no tiene muestras de personal válidas"
                )
            val destino = File(directorioConfig.obtener(), masReciente)
            ftp.descargar(RutaMuestraRemota.rutaPersonal(anio, masReciente), destino)
            MuestraDescargada(masReciente, destino)
        }

    /** Nombre de personal válido con mayor fecha de generación (RF-33). */
    private fun seleccionarMasReciente(nombres: List<String>, anio: Int): String? =
        nombres.mapNotNull { nombre ->
            runCatching { parser.parse(nombre, anio) }
                .getOrNull()
                ?.takeIf { it.tipo == TipoMuestra.PERSONAL }
                ?.fechaGeneracion
                ?.let { fecha -> nombre to fecha }
        }.maxByOrNull { it.second }?.first
}
