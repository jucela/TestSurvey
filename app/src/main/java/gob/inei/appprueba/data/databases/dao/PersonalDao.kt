package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.CredencialPersonal
import gob.inei.appprueba.data.databases.entities.PersonalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PersonalEntity>)

    @Query("DELETE FROM PERSONAL")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM PERSONAL")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM PERSONAL")
    fun observarCount(): Flow<Int>

    @Query(
        "SELECT p.USUARIO AS USUARIO, p.CLAVE AS CLAVE, p.ACTIVO AS ACTIVO, " +
            "p.NOMBRE AS NOMBRE, p.APEPATERNO AS APEPATERNO, p.APEMATERNO AS APEMATERNO, " +
            "c.NOMCARGO AS NOMCARGO " +
            "FROM PERSONAL p LEFT JOIN CARGO c ON p.CARGO = c.CARGO " +
            "WHERE p.USUARIO = :usuario LIMIT 1"
    )
    suspend fun buscarCredencial(usuario: String): CredencialPersonal?
}
