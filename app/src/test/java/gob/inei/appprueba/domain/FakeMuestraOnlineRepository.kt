package gob.inei.appprueba.domain

import gob.inei.appprueba.domain.entities.MuestraDescargada
import gob.inei.appprueba.domain.entities.MuestraException
import gob.inei.appprueba.domain.entities.TipoError
import gob.inei.appprueba.domain.repositories.MuestraOnlineRepository
import java.io.File

class FakeMuestraOnlineRepository : MuestraOnlineRepository {

    var viviendasLanzan: MuestraException? = null
    var personalLanzan: MuestraException? = null
    var nombreViviendas = "Enaho2026_Muestra_Odei_01_Mes_08.zip"
    var nombrePersonal = "Personal_2026_Tablet-2026-10-13.zip"
    var odeiSolicitada: String? = null
    var mesSolicitada: String? = null
    var personalLlamadas = 0

    override suspend fun obtenerViviendas(odei: String, mes: String): File {
        odeiSolicitada = odei
        mesSolicitada = mes
        viviendasLanzan?.let { throw it }
        return File("C:/tmp", nombreViviendas)
    }

    override suspend fun obtenerPersonalMasReciente(): MuestraDescargada {
        personalLlamadas++
        personalLanzan?.let { throw it }
        return MuestraDescargada(nombrePersonal, File("C:/tmp", nombrePersonal))
    }

    companion object {
        fun sinMuestras(): FakeMuestraOnlineRepository = FakeMuestraOnlineRepository().apply {
            personalLanzan = MuestraException(
                TipoError.ARCHIVO_REMOTO_AUSENTE,
                "El servidor no tiene muestras de personal válidas"
            )
        }
    }
}
