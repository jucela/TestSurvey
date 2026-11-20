package gob.inei.appprueba.domain.repositories

import gob.inei.appprueba.domain.entities.CatalogoMuestra
import gob.inei.appprueba.domain.entities.OpcionMuestra

/** Catálogos ODEI/MES para el modo Online (RF-30). */
interface CatalogoMuestraRepository {

    /** ODEIs de la tabla `ODEI` (código + nombre) (RF-30). */
    suspend fun odeis(): List<OpcionMuestra>

    /** Meses de la tabla `MES` (código + nombre) (RF-30). */
    suspend fun meses(): List<OpcionMuestra>

    /** Ambos catálogos en una sola consulta (RF-30). */
    suspend fun catalogo(): CatalogoMuestra
}
