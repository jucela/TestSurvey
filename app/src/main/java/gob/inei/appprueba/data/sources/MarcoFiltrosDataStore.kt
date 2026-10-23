package gob.inei.appprueba.data.sources

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import gob.inei.appprueba.domain.entities.MarcoFiltros
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Persistencia de los filtros de Marco por usuario autenticado (RF-13). */
class MarcoFiltrosDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val sessionDataStore: SessionDataStore
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observar(): Flow<MarcoFiltros> =
        sessionDataStore.observeUltimoUsuario()
            .flatMapLatest { usuario ->
                val claveUsuario = usuario?.trim().orEmpty()
                if (claveUsuario.isEmpty()) {
                    flowOf(MarcoFiltros())
                } else {
                    dataStore.data.map { preferencias -> leer(claveUsuario, preferencias) }
                }
            }
            .distinctUntilChanged()

    suspend fun guardar(filtros: MarcoFiltros) {
        val usuario = sessionDataStore.getUltimoUsuario()?.trim().orEmpty()
        if (usuario.isEmpty()) return
        dataStore.edit { preferencias ->
            preferencias[claveSoloActivos(usuario)] = filtros.soloActivos
            guardarTexto(preferencias, claveOdei(usuario), filtros.odei)
            guardarTexto(preferencias, claveMes(usuario), filtros.mes)
            guardarTexto(preferencias, clavePeriodo(usuario), filtros.periodo)
            guardarTexto(preferencias, claveConglomerado(usuario), filtros.conglomerado)
        }
    }

    private fun leer(usuario: String, preferencias: Preferences): MarcoFiltros = MarcoFiltros(
        soloActivos = preferencias[claveSoloActivos(usuario)] ?: false,
        odei = preferencias[claveOdei(usuario)]?.takeIf { it.isNotBlank() },
        mes = preferencias[claveMes(usuario)]?.takeIf { it.isNotBlank() },
        periodo = preferencias[clavePeriodo(usuario)]?.takeIf { it.isNotBlank() },
        conglomerado = preferencias[claveConglomerado(usuario)]?.takeIf { it.isNotBlank() }
    )

    private fun guardarTexto(
        preferencias: androidx.datastore.preferences.core.MutablePreferences,
        clave: Preferences.Key<String>,
        valor: String?
    ) {
        if (valor.isNullOrBlank()) {
            preferencias.remove(clave)
        } else {
            preferencias[clave] = valor
        }
    }

    private fun claveSoloActivos(usuario: String) =
        booleanPreferencesKey("$PREFIJO${usuario}_solo_activos")

    private fun claveOdei(usuario: String) =
        stringPreferencesKey("$PREFIJO${usuario}_odei")

    private fun claveMes(usuario: String) =
        stringPreferencesKey("$PREFIJO${usuario}_mes")

    private fun clavePeriodo(usuario: String) =
        stringPreferencesKey("$PREFIJO${usuario}_periodo")

    private fun claveConglomerado(usuario: String) =
        stringPreferencesKey("$PREFIJO${usuario}_conglomerado")

    private companion object {
        const val PREFIJO = "marco_filtros_"
    }
}
