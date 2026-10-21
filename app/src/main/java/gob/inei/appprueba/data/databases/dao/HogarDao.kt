package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.HogarEntity

@Dao
interface HogarDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<HogarEntity>)

    @Query(
        "DELETE FROM CAMPO_HOGAR WHERE CONGLOME IN " +
            "(SELECT CONGLOME FROM CAMPO_CONGLOMERADO WHERE ANHO = :anho AND MES = :mes AND ODEI = :odei)"
    )
    suspend fun deleteCombinacion(anho: String, mes: String, odei: String)

    @Query("SELECT COUNT(*) FROM CAMPO_HOGAR")
    suspend fun count(): Int

    @Query("SELECT * FROM CAMPO_HOGAR WHERE CONGLOME = :conglome AND VIVIENDA = :vivienda ORDER BY HOGAR")
    suspend fun obtenerPorConglomeradoVivienda(conglome: String, vivienda: String): List<HogarEntity>
}