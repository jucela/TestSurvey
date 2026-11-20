package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.ViviendaEntity

@Dao
interface ViviendaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ViviendaEntity>)

    @Query("SELECT COUNT(*) FROM CAMPO_VIVIENDA")
    suspend fun count(): Int

    @Query("SELECT * FROM CAMPO_VIVIENDA ORDER BY CONGLOME, VIVIENDA")
    suspend fun listar(): List<ViviendaEntity>
}