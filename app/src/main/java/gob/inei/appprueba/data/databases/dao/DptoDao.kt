package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.DptoEntity

@Dao
interface DptoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DptoEntity>)

    @Query("SELECT COUNT(*) FROM DPTO")
    suspend fun count(): Int
}
