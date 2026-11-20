package gob.inei.appprueba.data.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Tabla `PERFIL` según docs/entities/PERSONAL_PERFIL_CARGO.sql. */
@Entity(
    tableName = "PERFIL",
    indices = [Index(value = ["NOMPERFIL"], unique = true)]
)
data class PerfilEntity(
    @PrimaryKey
    @ColumnInfo(name = "CODPERFIL") val codPerfil: String,
    @ColumnInfo(name = "NOMPERFIL") val nomPerfil: String
)
