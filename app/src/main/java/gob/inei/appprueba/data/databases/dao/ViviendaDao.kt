package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.ViviendaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ViviendaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ViviendaEntity>)

    @Query(
        "DELETE FROM CAMPO_VIVIENDA WHERE CONGLOME IN " +
            "(SELECT CONGLOME FROM CAMPO_CONGLOMERADO WHERE ANHO = :anho AND MES = :mes AND ODEI = :odei)"
    )
    suspend fun deleteCombinacion(anho: String, mes: String, odei: String)

    @Query("SELECT COUNT(*) FROM CAMPO_VIVIENDA")
    suspend fun count(): Int

    @Query("SELECT * FROM CAMPO_VIVIENDA WHERE CONGLOME = :conglome ORDER BY NROVIVIENDA")
    suspend fun listarPorConglomerado(conglome: String): List<ViviendaEntity>

    @Query("SELECT * FROM CAMPO_VIVIENDA WHERE CONGLOME = :conglome AND (VIVIENDA LIKE :query OR NROVIVIENDA LIKE :query OR NOMBREVIA LIKE :query) ORDER BY NROVIVIENDA")
    suspend fun buscarPorConglomerado(conglome: String, query: String): List<ViviendaEntity>

    @Query("SELECT COUNT(*) FROM CAMPO_VIVIENDA WHERE CONGLOME = :conglome")
    suspend fun contarPorConglomerado(conglome: String): Int

    @Query("SELECT COUNT(*) FROM CAMPO_VIVIENDA_MARCO WHERE CONGLOME = :conglome AND TIPOVIVIENDA = :tipoVivienda")
    suspend fun contarPorConglomeradoYTipo(conglome: String, tipoVivienda: String): Int

    @Query(
        "SELECT " +
            "vm.CONGLOME AS conglome, " +
            "vm.VIVIENDA AS vivienda, " +
            "vm.NROVIVIENDA AS nroVivienda, " +
            "vm.TIPOVIVIENDA AS tipoVivienda, " +
            "vm.ACTIVO AS activo, " +
            "v.NROREEMPLAZO AS nroReemplazo, " +
            "v.ESREEMPLAZO AS esReemplazo, " +
            "h.HOGAR AS hogar, " +
            "h.RESFIN AS resFin, " +
            "v.NOMBREVIA AS nombreViaV, " +
            "vm.NOMBREVIA AS nombreViaM, " +
            "v.PUERTA AS puertaV, " +
            "vm.PUERTA AS puertaM, " +
            "v.INTERIOR AS interiorV, " +
            "vm.INTERIOR AS interiorM, " +
            "v.PISO AS pisoV, " +
            "vm.PISO AS pisoM, " +
            "v.MZ AS mzV, " +
            "vm.MZ AS mzM, " +
            "v.LOTE AS loteV, " +
            "vm.LOTE AS loteM " +
            "FROM CAMPO_VIVIENDA_MARCO vm " +
            "JOIN CAMPO_CONGLOMERADO c ON vm.CONGLOME = c.CONGLOME " +
            "LEFT JOIN CAMPO_VIVIENDA v ON vm.CONGLOME = v.CONGLOME AND vm.VIVIENDA = v.VIVIENDA " +
            "LEFT JOIN CAMPO_HOGAR h ON vm.CONGLOME = h.CONGLOME AND vm.VIVIENDA = h.VIVIENDA " +
            "WHERE vm.CONGLOME = :conglome " +
            "AND (:soloActivos = 0 OR vm.ACTIVO = 1) " +
            "ORDER BY vm.NROVIVIENDA " +
            "LIMIT :limite"
    )
    suspend fun obtenerDetalleConglomerado(
        conglome: String,
        soloActivos: Int,
        limite: Int
    ): List<ViviendaDetalleRow>

    data class ViviendaDetalleRow(
        val conglome: String,
        val vivienda: String,
        val nroVivienda: String?,
        val tipoVivienda: String?,
        val activo: Int?,
        val nroReemplazo: String?,
        val esReemplazo: String?,
        val hogar: String?,
        val resFin: String?,
        val nombreViaV: String?,
        val nombreViaM: String?,
        val puertaV: String?,
        val puertaM: String?,
        val interiorV: String?,
        val interiorM: String?,
        val pisoV: String?,
        val pisoM: String?,
        val mzV: String?,
        val mzM: String?,
        val loteV: String?,
        val loteM: String?
    )
}