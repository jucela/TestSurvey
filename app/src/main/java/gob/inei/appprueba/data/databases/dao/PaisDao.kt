package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.PaisEntity

@Dao
interface PaisDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PaisEntity>)

    @Query("SELECT COUNT(*) FROM PAIS")
    suspend fun count(): Int
}
