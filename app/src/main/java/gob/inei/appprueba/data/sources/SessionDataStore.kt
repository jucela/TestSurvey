package gob.inei.appprueba.data.sources

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

internal val Context.sesionDataStore by preferencesDataStore(name = "sesion")

/** Estado de sesión y preferencias de login, según RF-05. */
class SessionDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    fun observeSesionActiva(): Flow<Boolean> =
        dataStore.data.map { it[CLAVE_SESION_ACTIVA] ?: false }

    suspend fun setSesionActiva(activa: Boolean) {
        dataStore.edit { it[CLAVE_SESION_ACTIVA] = activa }
    }

    fun observeUltimoUsuario(): Flow<String?> =
        dataStore.data.map { it[CLAVE_ULTIMO_USUARIO] }

    suspend fun getUltimoUsuario(): String? =
        dataStore.data.first()[CLAVE_ULTIMO_USUARIO]

    suspend fun setUltimoUsuario(usuario: String) {
        dataStore.edit { it[CLAVE_ULTIMO_USUARIO] = usuario }
    }

    companion object {
        private val CLAVE_SESION_ACTIVA = booleanPreferencesKey("sesion_activa")
        private val CLAVE_ULTIMO_USUARIO = stringPreferencesKey("ultimo_usuario")
    }
}
