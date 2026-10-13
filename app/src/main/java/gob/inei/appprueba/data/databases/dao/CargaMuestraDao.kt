package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.CargaMuestraEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CargaMuestraDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CargaMuestraEntity): Long

    @Query("DELETE FROM CARGA_MUESTRA WHERE tipo = :tipo")
    suspend fun deletePorTipo(tipo: String)

    @Query(
        "DELETE FROM CARGA_MUESTRA WHERE tipo = :tipo AND anho = :anho AND odei = :odei AND mes = :mes"
    )
    suspend fun deleteCombinacion(tipo: String, anho: String, odei: String, mes: String)

    @Query("SELECT MAX(fechaHora) FROM CARGA_MUESTRA WHERE tipo = :tipo")
    suspend fun ultimaFecha(tipo: String): Long?

    @Query("SELECT MAX(fechaHora) FROM CARGA_MUESTRA WHERE tipo = :tipo")
    fun observarUltimaFecha(tipo: String): Flow<Long?>

    @Query("SELECT COUNT(*) FROM CARGA_MUESTRA")
    suspend fun count(): Int
}
