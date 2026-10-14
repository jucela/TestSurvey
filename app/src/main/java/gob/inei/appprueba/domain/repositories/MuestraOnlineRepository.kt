package gob.inei.appprueba.domain.repositories

import gob.inei.appprueba.domain.entities.MuestraDescargada
import java.io.File

/**
 * Acceso remoto a las muestras publicadas en el servidor (modo Online,
 * RF-32/RF-33/RF-35). El repositorio resuelve la ruta remota, descarga el ZIP al
 * directorio `config` del aplicativo y devuelve el archivo local listo para el
 * pipeline de carga. No expone host ni credenciales.
 */
interface MuestraOnlineRepository {

    /**
     * Descarga la muestra de viviendas de la combinación Año+[odei]+[mes]
     * (códigos `01..`) y devuelve el ZIP local (RF-32/RF-35).
     */
    suspend fun obtenerViviendas(odei: String, mes: String): File

    /**
     * Descarga la muestra de personal de fecha de generación más reciente
     * (RF-33/RF-35). Lanza `MuestraException` si el servidor no tiene muestras
     * válidas.
     */
    suspend fun obtenerPersonalMasReciente(): MuestraDescargada
}
