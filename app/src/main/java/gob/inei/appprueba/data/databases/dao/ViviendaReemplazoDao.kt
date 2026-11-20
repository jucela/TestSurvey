package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.ViviendaReemplazoEntity

@Dao
interface ViviendaReemplazoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ViviendaReemplazoEntity>)

    @Query(
        "DELETE FROM CAMPO_VIVIENDA_REEMPLAZO WHERE CONGLOME IN " +
            "(SELECT CONGLOME FROM CAMPO_CONGLOMERADO WHERE ANHO = :anho AND MES = :mes AND ODEI = :odei)"
    )
    suspend fun deleteCombinacion(anho: String, mes: String, odei: String)

    @Query("SELECT COUNT(*) FROM CAMPO_VIVIENDA_REEMPLAZO")
    suspend fun count(): Int
}
