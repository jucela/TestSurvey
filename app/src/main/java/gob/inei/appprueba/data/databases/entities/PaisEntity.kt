package gob.inei.appprueba.data.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Tabla `PAIS` según docs/entities/ODEI_DPTO_PROV_DISTRITO_PAIS.sql. */
@Entity(tableName = "PAIS")
data class PaisEntity(
    @PrimaryKey
    @ColumnInfo(name = "CODPAIS") val codPais: String,
    @ColumnInfo(name = "NOMPAIS") val nomPais: String?,
    @ColumnInfo(name = "CODCONTINENTE") val codContinente: String?,
    @ColumnInfo(name = "USUCRE") val usuCre: String?,
    @ColumnInfo(name = "FECCRE") val fecCre: String?,
    @ColumnInfo(name = "USUREG") val usuReg: String?,
    @ColumnInfo(name = "FECREG") val fecReg: String?,
    @ColumnInfo(name = "FECENV") val fecEnv: String?
)
