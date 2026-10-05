package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.AnswerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnswerDao {

    @Query("SELECT * FROM answers")
    fun observeAll(): Flow<List<AnswerEntity>>

    @Query("SELECT * FROM answers")
    suspend fun getAll(): List<AnswerEntity>

    @Query("SELECT * FROM answers WHERE preguntaId = :id")
    suspend fun getById(id: String): AnswerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: AnswerEntity)

    @Query("DELETE FROM answers")
    suspend fun deleteAll()
}
