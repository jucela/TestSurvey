package gob.inei.appprueba.data.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Tabla `ODEI` según docs/entities/ODEI_DPTO_PROV_DISTRITO_PAIS.sql. */
@Entity(
    tableName = "ODEI",
    foreignKeys = [
        ForeignKey(
            entity = DptoEntity::class,
            parentColumns = ["CCDD"],
            childColumns = ["CCDD"],
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index(value = ["CODODEI"], unique = true), Index("CCDD")]
)
data class OdeiEntity(
    @PrimaryKey
    @ColumnInfo(name = "ID") val id: Int,
    @ColumnInfo(name = "CODODEI") val codOdei: String,
    @ColumnInfo(name = "NOMODEI") val nomOdei: String,
    @ColumnInfo(name = "CCDD") val ccdd: String?,
    @ColumnInfo(name = "USUCRE") val usuCre: String?,
    @ColumnInfo(name = "FECCRE") val fecCre: String?,
    @ColumnInfo(name = "USUREG") val usuReg: String?,
    @ColumnInfo(name = "FECREG") val fecReg: String?,
    @ColumnInfo(name = "FECENV") val fecEnv: String?
)
