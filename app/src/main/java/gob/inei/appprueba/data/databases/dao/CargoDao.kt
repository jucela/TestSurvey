package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.CargoEntity

@Dao
interface CargoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CargoEntity>)

    @Query("DELETE FROM CARGO")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM CARGO")
    suspend fun count(): Int
}
