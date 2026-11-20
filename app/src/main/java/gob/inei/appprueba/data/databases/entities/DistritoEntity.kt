package gob.inei.appprueba.data.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Tabla `DISTRITO` según docs/entities/ODEI_DPTO_PROV_DISTRITO_PAIS.sql. */
@Entity(
    tableName = "DISTRITO",
    primaryKeys = ["CCDD", "CCPP", "CCDI"],
    foreignKeys = [
        ForeignKey(
            entity = ProvEntity::class,
            parentColumns = ["CCDD", "CCPP"],
            childColumns = ["CCDD", "CCPP"],
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("CCDD", "CCPP")]
)
data class DistritoEntity(
    @ColumnInfo(name = "CCDD") val ccdd: String,
    @ColumnInfo(name = "CCPP") val ccpp: String,
    @ColumnInfo(name = "CCDI") val ccdi: String,
    @ColumnInfo(name = "NOMBRE_DIST") val nombreDist: String?,
    @ColumnInfo(name = "NOMBRE_CA") val nombreCa: String?,
    @ColumnInfo(name = "CODODEI") val codOdei: String?,
    @ColumnInfo(name = "ETAPA") val etapa: String?
)
