package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.ConglomeradoEntity
import kotlinx.coroutines.flow.Flow

/** Proyección ODEI/mes para las tarjetas informativas (RF-27). */
data class OdeiMesRow(
    val odei: String,
    val nombreOdei: String?,
    val mes: String?
)

@Dao
interface ConglomeradoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ConglomeradoEntity>)

    @Query("DELETE FROM CAMPO_CONGLOMERADO WHERE ANHO = :anho AND MES = :mes AND ODEI = :odei")
    suspend fun deleteCombinacion(anho: String, mes: String, odei: String)

    @Query("SELECT * FROM CAMPO_CONGLOMERADO WHERE ANHO = :anho AND MES = :mes AND ODEI = :odei")
    suspend fun obtenerPorCombinacion(anho: String, mes: String, odei: String): List<ConglomeradoEntity>

    @Query("DELETE FROM CAMPO_CONGLOMERADO WHERE CONGLOME IN (:conglomes)")
    suspend fun eliminarPorConglomes(conglomes: List<String>)

    @Query("SELECT COUNT(*) FROM CAMPO_CONGLOMERADO WHERE ANHO = :anho AND MES = :mes AND ODEI = :odei")
    suspend fun contarCombinacion(anho: String, mes: String, odei: String): Int

    @Query("SELECT COUNT(*) FROM CAMPO_CONGLOMERADO")
    suspend fun count(): Int

    @Query(
        "SELECT c.ODEI AS odei, o.NOMODEI AS nombreOdei, c.MES AS mes " +
            "FROM CAMPO_CONGLOMERADO c LEFT JOIN ODEI o ON c.ODEI = o.CODODEI " +
            "ORDER BY c.ODEI, c.MES"
    )
    fun observarOdeiMeses(): Flow<List<OdeiMesRow>>
}
