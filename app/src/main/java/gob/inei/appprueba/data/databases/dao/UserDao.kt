package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.UserEntity

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE usuario = :usuario")
    suspend fun getByUsuario(usuario: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: UserEntity)

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}
