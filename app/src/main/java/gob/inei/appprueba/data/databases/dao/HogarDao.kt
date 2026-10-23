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

    @Query("SELECT COUNT(*) FROM CAMPO_HOGAR")
    suspend fun count(): Int

    @Query("SELECT * FROM CAMPO_HOGAR ORDER BY CONGLOME, VIVIENDA, HOGAR")
    suspend fun listar(): List<HogarEntity>
}