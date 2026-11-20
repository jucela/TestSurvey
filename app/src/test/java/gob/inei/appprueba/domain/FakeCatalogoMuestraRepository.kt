package gob.inei.appprueba.domain

import gob.inei.appprueba.domain.entities.CatalogoMuestra
import gob.inei.appprueba.domain.entities.OpcionMuestra
import gob.inei.appprueba.domain.repositories.CatalogoMuestraRepository

class FakeCatalogoMuestraRepository : CatalogoMuestraRepository {

    var catalogo: CatalogoMuestra = CatalogoMuestra(
        odeis = listOf(OpcionMuestra("01", "AMAZONAS"), OpcionMuestra("11", "ICA")),
        meses = listOf(OpcionMuestra("01", "Enero"), OpcionMuestra("08", "Agosto"))
    )

    override suspend fun odeis(): List<OpcionMuestra> = catalogo.odeis

    override suspend fun meses(): List<OpcionMuestra> = catalogo.meses

    override suspend fun catalogo(): CatalogoMuestra = catalogo
}
