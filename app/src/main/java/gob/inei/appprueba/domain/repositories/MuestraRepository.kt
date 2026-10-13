package gob.inei.appprueba.domain.repositories

import gob.inei.appprueba.domain.entities.EstadoMuestraPersonal
import gob.inei.appprueba.domain.entities.EstadoMuestraViviendas
import gob.inei.appprueba.domain.entities.ResultadoArchivo
import kotlinx.coroutines.flow.Flow

interface MuestraRepository {

    /**
     * Procesa un único ZIP (personal o viviendas) de forma independiente: resuelve
     * el nombre, detecta el tipo, valida y carga (RF-14..RF-25). Nunca lanza por
     * reglas de negocio: devuelve el [ResultadoArchivo] con el estado alcanzado.
     */
    suspend fun cargar(uriZip: String): ResultadoArchivo

    /**
     * Resuelve el nombre visible de cada `Uri` conservando el orden (RF-14). Usa
     * el último segmento de la ruta como respaldo si no puede obtener `DISPLAY_NAME`.
     */
    suspend fun nombresDe(urisZip: List<String>): List<String>

    /** Estado de la muestra de personal para la tarjeta informativa (RF-27). */
    fun observarMuestraPersonal(): Flow<EstadoMuestraPersonal>

    /** Estado de la muestra de viviendas para la tarjeta informativa (RF-27). */
    fun observarMuestraViviendas(): Flow<EstadoMuestraViviendas>
}
