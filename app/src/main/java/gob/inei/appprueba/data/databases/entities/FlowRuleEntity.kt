package gob.inei.appprueba.data.databases.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flow_rules")
data class FlowRuleEntity(
    @PrimaryKey val id: Long,
    val anio: Int,
    val origen: String,
    val condicion: String,
    val accion: String,
    val destino: String,
    val instruccion: String?
)
