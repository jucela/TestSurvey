package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.Cap100PanelEntity

@Dao
interface Cap100PanelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Cap100PanelEntity>)

    @Query(
        "DELETE FROM CAP100PANEL WHERE CONGLOME IN " +
            "(SELECT CONGLOME FROM CAMPO_CONGLOMERADO WHERE ANHO = :anho AND MES = :mes AND ODEI = :odei)"
    )
    suspend fun deleteCombinacion(anho: String, mes: String, odei: String)

    @Query("SELECT COUNT(*) FROM CAP100PANEL")
    suspend fun count(): Int
}
