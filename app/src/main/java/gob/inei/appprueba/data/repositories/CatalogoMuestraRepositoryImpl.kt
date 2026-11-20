package gob.inei.appprueba.data.repositories

import gob.inei.appprueba.data.databases.dao.MesDao
import gob.inei.appprueba.data.databases.dao.OdeiDao
import gob.inei.appprueba.domain.entities.CatalogoMuestra
import gob.inei.appprueba.domain.entities.OpcionMuestra
import gob.inei.appprueba.domain.repositories.CatalogoMuestraRepository
import javax.inject.Inject
import javax.inject.Singleton

/** Implementación de los catálogos ODEI/MES desde Room (RF-30, DP-20). */
@Singleton
class CatalogoMuestraRepositoryImpl @Inject constructor(
    private val odeiDao: OdeiDao,
    private val mesDao: MesDao
) : CatalogoMuestraRepository {

    override suspend fun odeis(): List<OpcionMuestra> =
        odeiDao.listar().map { OpcionMuestra(it.codOdei, it.nomOdei) }

    override suspend fun meses(): List<OpcionMuestra> =
        mesDao.listar().map { OpcionMuestra(it.codigo, it.nombre ?: it.codigo) }

    override suspend fun catalogo(): CatalogoMuestra =
        CatalogoMuestra(odeis = odeis(), meses = meses())
}
