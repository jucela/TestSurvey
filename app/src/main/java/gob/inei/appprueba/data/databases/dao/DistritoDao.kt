package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.DistritoEntity

@Dao
interface DistritoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DistritoEntity>)

    @Query("SELECT COUNT(*) FROM DISTRITO")
    suspend fun count(): Int
}
