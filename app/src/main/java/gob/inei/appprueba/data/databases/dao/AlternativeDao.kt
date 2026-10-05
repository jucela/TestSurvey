package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.AlternativeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlternativeDao {

    @Query("SELECT * FROM alternatives ORDER BY pregunta, orden")
    fun observeAll(): Flow<List<AlternativeEntity>>

    @Query("SELECT * FROM alternatives ORDER BY pregunta, orden")
    suspend fun getAll(): List<AlternativeEntity>

    @Query("SELECT * FROM alternatives WHERE pregunta = :pregunta ORDER BY orden")
    suspend fun getByPregunta(pregunta: String): List<AlternativeEntity>

    @Query("SELECT COUNT(*) FROM alternatives")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<AlternativeEntity>)

    @Query("DELETE FROM alternatives")
    suspend fun deleteAll()
}
