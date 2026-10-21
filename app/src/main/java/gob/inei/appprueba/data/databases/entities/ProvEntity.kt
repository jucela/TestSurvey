package gob.inei.appprueba.data.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Tabla `PROV` según docs/entities/ODEI_DPTO_PROV_DISTRITO_PAIS.sql. */
@Entity(
    tableName = "PROV",
    primaryKeys = ["CCDD", "CCPP"],
    foreignKeys = [
        ForeignKey(
            entity = DptoEntity::class,
            parentColumns = ["CCDD"],
            childColumns = ["CCDD"],
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("CCDD")]
)
data class ProvEntity(
    @ColumnInfo(name = "CCDD") val ccdd: String,
    @ColumnInfo(name = "CCPP") val ccpp: String,
    @ColumnInfo(name = "NOMBREPV") val nombrePv: String
)
