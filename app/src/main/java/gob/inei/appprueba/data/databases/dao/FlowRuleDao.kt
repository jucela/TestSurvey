package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gob.inei.appprueba.data.databases.entities.FlowRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlowRuleDao {

    @Query("SELECT * FROM flow_rules ORDER BY id")
    fun observeAll(): Flow<List<FlowRuleEntity>>

    @Query("SELECT * FROM flow_rules ORDER BY id")
    suspend fun getAll(): List<FlowRuleEntity>

    @Query("SELECT COUNT(*) FROM flow_rules")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FlowRuleEntity>)

    @Query("DELETE FROM flow_rules")
    suspend fun deleteAll()
}
