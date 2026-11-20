package gob.inei.appprueba.data.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Tabla `DPTO` según docs/entities/ODEI_DPTO_PROV_DISTRITO_PAIS.sql. */
@Entity(tableName = "DPTO")
data class DptoEntity(
    @PrimaryKey
    @ColumnInfo(name = "CCDD") val ccdd: String,
    @ColumnInfo(name = "NOMBREDD") val nombreDd: String
)
