package gob.inei.appprueba.data.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Tabla `MES` según docs/entities/MES.sql (modo Online, RF-30). */
@Entity(tableName = "MES")
data class MesEntity(
    @PrimaryKey
    @ColumnInfo(name = "CODIGO") val codigo: String,
    @ColumnInfo(name = "NOMBRE") val nombre: String?
)
