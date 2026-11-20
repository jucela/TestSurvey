package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.PeriodoEntity

@Dao
interface PeriodoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PeriodoEntity>)

    @Query("SELECT COUNT(*) FROM PERIODO")
    suspend fun count(): Int

    @Query("SELECT * FROM PERIODO ORDER BY CODPER")
    suspend fun listar(): List<PeriodoEntity>
}
