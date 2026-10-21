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

/** Conglomerado con área geográfica calculada para filtros (Marco). */
data class ConglomeradoConArea(
    val conglome: String,
    val areaGeografica: String
)

@Dao
interface ConglomeradoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ConglomeradoEntity>)

    @Query("DELETE FROM CAMPO_CONGLOMERADO WHERE ANHO = :anho AND MES = :mes AND ODEI = :odei")
    suspend fun deleteCombinacion(anho: String, mes: String, odei: String)

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

    @Query(
        "SELECT " +
            "c.CONGLOME AS conglome, " +
            "CASE " +
            "   WHEN c.TIPOSELECCION IN ('1','2') THEN 'URBANO->NO PANEL' " +
            "   WHEN c.TIPOSELECCION = '3' AND c.ESTRATO = '6' THEN 'SEMI-RURAL->PANEL' " +
            "   WHEN c.TIPOSELECCION = '3' AND c.ESTRATO IN ('1','2','3','4','5') THEN 'URBANO->PANEL' " +
            "   WHEN c.TIPOSELECCION = '3' AND CAST(c.ESTRATO AS INTEGER) >= 7 THEN 'RURAL->PANEL' " +
            "   WHEN c.TIPOSELECCION = '4' THEN 'SEMI-RURAL->NO PANEL' " +
            "   WHEN c.TIPOSELECCION = '5' THEN 'RURAL->NO PANEL' " +
            "   ELSE 'DESCONOCIDO' " +
            "END AS areaGeografica " +
            "FROM CAMPO_CONGLOMERADO c " +
            "WHERE c.ANHO = :anho AND c.ODEI = :odei AND c.MES = :mes " +
            "ORDER BY c.CONGLOME"
    )
    suspend fun listarConglomeradosFiltrados(anho: String, odei: String, mes: String): List<ConglomeradoConArea>

    @Query("SELECT * FROM CAMPO_CONGLOMERADO WHERE CONGLOME = :conglome LIMIT 1")
    suspend fun obtenerPorConglome(conglome: String): ConglomeradoEntity?
}
