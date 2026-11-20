package gob.inei.appprueba.data.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Tabla `PERIODO` según docs/entities/CONGLOMERADO_MARCO_REEMPLAZO_PERSONA_CAP100.sql. */
@Entity(tableName = "PERIODO")
data class PeriodoEntity(
    @PrimaryKey
    @ColumnInfo(name = "CODPER") val codPer: String,
    @ColumnInfo(name = "NOMPER") val nomPer: String?
)
