package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.PersonaPanelEntity

@Dao
interface PersonaPanelDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(items: List<PersonaPanelEntity>)

    @Query(
        "DELETE FROM PERSONA_PANEL WHERE CONGLOME IN " +
            "(SELECT CONGLOME FROM CAMPO_CONGLOMERADO WHERE ANHO = :anho AND MES = :mes AND ODEI = :odei)"
    )
    suspend fun deleteCombinacion(anho: String, mes: String, odei: String)

    @Query("SELECT COUNT(*) FROM PERSONA_PANEL")
    suspend fun count(): Int
}
