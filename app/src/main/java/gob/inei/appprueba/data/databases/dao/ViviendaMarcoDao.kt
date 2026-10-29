package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.ViviendaMarcoEntity

@Dao
interface ViviendaMarcoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ViviendaMarcoEntity>)

    @Query(
        "DELETE FROM CAMPO_VIVIENDA_MARCO WHERE CONGLOME IN " +
            "(SELECT CONGLOME FROM CAMPO_CONGLOMERADO WHERE ANHO = :anho AND MES = :mes AND ODEI = :odei)"
    )
    suspend fun deleteCombinacion(anho: String, mes: String, odei: String)

    @Query(
        "SELECT * FROM CAMPO_VIVIENDA_MARCO WHERE CONGLOME IN " +
            "(SELECT CONGLOME FROM CAMPO_CONGLOMERADO WHERE ANHO = :anho AND MES = :mes AND ODEI = :odei)"
    )
    suspend fun obtenerPorMuestra(anho: String, mes: String, odei: String): List<ViviendaMarcoEntity>

    @Query("UPDATE CAMPO_VIVIENDA_MARCO SET activo = :activo WHERE CONGLOME = :conglome AND VIVIENDA = :vivienda")
    suspend fun actualizarActivo(conglome: String, vivienda: String, activo: Int)

    @Query("UPDATE CAMPO_VIVIENDA_MARCO SET activo = 1 WHERE CONGLOME = :conglome AND VIVIENDA = :vivienda")
    suspend fun activar(conglome: String, vivienda: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOVerActualizar(items: List<ViviendaMarcoEntity>)

    @Query("SELECT COUNT(*) FROM CAMPO_VIVIENDA_MARCO")
    suspend fun count(): Int
}
