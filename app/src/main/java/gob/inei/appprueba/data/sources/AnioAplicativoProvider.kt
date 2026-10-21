package gob.inei.appprueba.data.sources

import gob.inei.appprueba.BuildConfig
import javax.inject.Inject

/**
 * Provee el año del aplicativo (RF-17, DP-12). Es una constante de build que se
 * usa para validar el año del nombre de archivo y la raíz `ENAHO<año>` del XML.
 */
class AnioAplicativoProvider @Inject constructor() {
    fun obtener(): Int = BuildConfig.ANIO_APLICATIVO
}
