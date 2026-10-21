package gob.inei.appprueba.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import gob.inei.appprueba.domain.repository.MarcoRepository

@Singleton
class MarcoPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private const val PREF_ACTIVOS = "marco_activos"
        private const val PREF_ODEI = "marco_odei"
        private const val PREF_MES = "marco_mes"
        private const val PREF_PERIODO = "marco_periodo"
        private const val PREF_CONGLOME = "marco_conglome"

        val ACTIVOS_KEY = booleanPreferencesKey("marco_activos")
        val ODEI_KEY = stringPreferencesKey("marco_odei")
        val MES_KEY = stringPreferencesKey("marco_mes")
        val PERIODO_KEY = stringPreferencesKey("marco_periodo")
        val CONGLOME_KEY = stringPreferencesKey("marco_conglome")
    }

    data class FiltrosGuardados(
        val activos: Boolean = true,
        val odei: String? = null,
        val mes: String? = null,
        val periodo: String? = null,
        val conglome: String? = null
    )

    val filtros: Flow<FiltrosGuardados> = dataStore.data.map { prefs ->
        FiltrosGuardados(
            activos = prefs[ACTIVOS_KEY] ?: true,
            odei = prefs[ODEI_KEY],
            mes = prefs[MES_KEY],
            periodo = prefs[PERIODO_KEY],
            conglome = prefs[CONGLOME_KEY]
        )
    }

    suspend fun guardar(
        activos: Boolean,
        odei: String?,
        mes: String?,
        periodo: String?,
        conglome: String?
    ) {
        dataStore.edit { prefs ->
            prefs[ACTIVOS_KEY] = activos
            odei?.let { prefs[ODEI_KEY] = it }
            mes?.let { prefs[MES_KEY] = it }
            periodo?.let { prefs[PERIODO_KEY] = it }
            conglome?.let { prefs[CONGLOME_KEY] = it }
        }
    }

    suspend fun obtener(): MarcoRepository.FiltrosGuardados {
        val prefs = dataStore.data.first()
        return MarcoRepository.FiltrosGuardados(
            activos = prefs[ACTIVOS_KEY] ?: true,
            odei = prefs[ODEI_KEY],
            mes = prefs[MES_KEY],
            periodo = prefs[PERIODO_KEY],
            conglome = prefs[CONGLOME_KEY]
        )
    }
}