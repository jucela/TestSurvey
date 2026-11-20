package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.ProvEntity

@Dao
interface ProvDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ProvEntity>)

    @Query("SELECT COUNT(*) FROM PROV")
    suspend fun count(): Int
}
