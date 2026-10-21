package gob.inei.appprueba.data.sources

import gob.inei.appprueba.BuildConfig
import javax.inject.Inject

/**
 * Provee la contraseña del ZIP de muestra (RF-04). El valor se inyecta en build
 * desde local.properties (no versionado); nunca se registra ni se muestra.
 */
class PasswordMuestraProvider @Inject constructor() {
    fun obtener(): String = BuildConfig.ZIP_PASSWORD
}
