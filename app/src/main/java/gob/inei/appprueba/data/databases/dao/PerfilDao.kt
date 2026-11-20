package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.PerfilEntity

@Dao
interface PerfilDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PerfilEntity>)

    @Query("DELETE FROM PERFIL")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM PERFIL")
    suspend fun count(): Int
}
