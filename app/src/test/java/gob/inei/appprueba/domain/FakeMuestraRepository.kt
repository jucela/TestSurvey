package gob.inei.appprueba.domain

import gob.inei.appprueba.domain.entities.EstadoArchivo
import gob.inei.appprueba.domain.entities.EstadoMuestraPersonal
import gob.inei.appprueba.domain.entities.EstadoMuestraViviendas
import gob.inei.appprueba.domain.entities.ModoCargaMuestra
import gob.inei.appprueba.domain.entities.ResultadoArchivo
import gob.inei.appprueba.domain.entities.TipoMuestra
import gob.inei.appprueba.domain.repositories.MuestraRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeMuestraRepository : MuestraRepository {

    var resultado: ResultadoArchivo =
        ResultadoArchivo("muestra.zip", EstadoArchivo.CARGADO, TipoMuestra.PERSONAL)
    var nombres: List<String> = emptyList()
    var personalState = MutableStateFlow(EstadoMuestraPersonal(false, null))
    var viviendasState =
        MutableStateFlow(EstadoMuestraViviendas(false, null, emptyList()))
    var llamadas = 0
    var ultimaUri: String? = null
    var ultimoModo: ModoCargaMuestra? = null

    override suspend fun cargar(uriZip: String, modo: ModoCargaMuestra): ResultadoArchivo {
        llamadas++
        ultimaUri = uriZip
        ultimoModo = modo
        return resultado
    }

    override suspend fun nombresDe(urisZip: List<String>): List<String> =
        if (nombres.isEmpty()) urisZip else nombres

    override fun observarMuestraPersonal(): Flow<EstadoMuestraPersonal> = personalState

    override fun observarMuestraViviendas(): Flow<EstadoMuestraViviendas> = viviendasState
}
