package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.entities.EstadoMuestraPersonal
import gob.inei.appprueba.domain.entities.EstadoMuestraViviendas
import gob.inei.appprueba.domain.repositories.MuestraRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/** Observa el estado de las muestras cargadas para las tarjetas informativas (RF-27). */
class ObservarMuestrasUseCase @Inject constructor(
    private val repository: MuestraRepository
) {
    fun personal(): Flow<EstadoMuestraPersonal> = repository.observarMuestraPersonal()

    fun viviendas(): Flow<EstadoMuestraViviendas> = repository.observarMuestraViviendas()
}
