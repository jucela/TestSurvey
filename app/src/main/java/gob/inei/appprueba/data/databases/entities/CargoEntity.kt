package gob.inei.appprueba.data.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Tabla `CARGO` según docs/entities/PERSONAL_PERFIL_CARGO.sql. */
@Entity(tableName = "CARGO")
data class CargoEntity(
    @PrimaryKey
    @ColumnInfo(name = "CARGO") val cargo: String,
    @ColumnInfo(name = "NOMCARGO") val nomCargo: String?,
    @ColumnInfo(name = "ABRCARGO") val abrCargo: String?
)
