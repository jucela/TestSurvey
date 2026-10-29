package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.OdeiEntity

@Dao
interface OdeiDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<OdeiEntity>)

    @Query("SELECT COUNT(*) FROM ODEI")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM ODEI WHERE CODODEI = :codOdei")
    suspend fun existe(codOdei: String): Int

    @Query("SELECT NOMODEI FROM ODEI WHERE CODODEI = :codOdei LIMIT 1")
    suspend fun nombreDe(codOdei: String): String?

    @Query("SELECT * FROM ODEI ORDER BY CODODEI")
    suspend fun listar(): List<OdeiEntity>
}
