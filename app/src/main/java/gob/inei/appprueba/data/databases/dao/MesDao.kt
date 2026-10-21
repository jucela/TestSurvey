package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.MesEntity

@Dao
interface MesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MesEntity>)

    @Query("SELECT COUNT(*) FROM MES")
    suspend fun count(): Int

    @Query("SELECT * FROM MES ORDER BY CODIGO")
    suspend fun listar(): List<MesEntity>
}
