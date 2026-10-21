package gob.inei.appprueba.data.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Tabla `CAMPO_HOGAR` según docs/entities/VIVIENDA_HOGAR.sql. */
@Entity(
    tableName = "CAMPO_HOGAR",
    primaryKeys = ["CONGLOME", "VIVIENDA", "HOGAR"],
    foreignKeys = [
        ForeignKey(
            entity = ViviendaEntity::class,
            parentColumns = ["CONGLOME", "VIVIENDA"],
            childColumns = ["CONGLOME", "VIVIENDA"],
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index("CONGLOME", "VIVIENDA")
    ]
)
data class HogarEntity(
    @ColumnInfo(name = "CONGLOME") val conglome: String,
    @ColumnInfo(name = "VIVIENDA") val vivienda: String,
    @ColumnInfo(name = "HOGAR") val hogar: String,
    @ColumnInfo(name = "HOGAR_ORDEN") val hogarOrden: String?,
    @ColumnInfo(name = "NOMBRE_JEFE") val nombreJefe: String?,
    @ColumnInfo(name = "APELLIDO_JEFE") val apellidoJefe: String?,
    @ColumnInfo(name = "APELLIDO2_JEFE") val apellido2Jefe: String?,
    @ColumnInfo(name = "FECRESREC") val fecResRec: String?,
    @ColumnInfo(name = "FECRESFIN") val fecResFin: String?,
    @ColumnInfo(name = "RESFIN") val resFin: String?,
    @ColumnInfo(name = "CODENC") val codEnc: String?,
    @ColumnInfo(name = "CODSUPLOC") val codSupLoc: String?,
    @ColumnInfo(name = "CODCOORD") val codCoord: String?,
    @ColumnInfo(name = "CODSUPNAC") val codSupNac: String?,
    @ColumnInfo(name = "HOGARENTRE") val hogarEntre: String?,
    @ColumnInfo(name = "P22") val p22: String?,
    @ColumnInfo(name = "P23") val p23: String?,
    @ColumnInfo(name = "P24A") val p24a: String?,
    @ColumnInfo(name = "P24B") val p24b: String?,
    @ColumnInfo(name = "P25") val p25: String?,
    @ColumnInfo(name = "P25_1") val p25_1: String?,
    @ColumnInfo(name = "P25_2") val p25_2: String?,
    @ColumnInfo(name = "P25_3") val p25_3: String?,
    @ColumnInfo(name = "P25_4") val p25_4: String?,
    @ColumnInfo(name = "P25_5") val p25_5: String?,
    @ColumnInfo(name = "PDA") val pda: String?,
    @ColumnInfo(name = "USUARIODIG") val usuarioDig: String?,
    @ColumnInfo(name = "FECHADIG") val fechaDig: String?,
    @ColumnInfo(name = "USUARIOMOD") val usuarioMod: String?,
    @ColumnInfo(name = "FECHAMOD") val fechaMod: String?,
    @ColumnInfo(name = "MOVIL") val movil: String?,
    @ColumnInfo(name = "HORAINIDIG") val horaIniDig: String?,
    @ColumnInfo(name = "HORAFINDIG") val horaFinDig: String?,
    @ColumnInfo(name = "COMPLETO") val completo: String?,
    @ColumnInfo(name = "USUCRE") val usuCre: String?,
    @ColumnInfo(name = "FECCRE") val fecCre: String?,
    @ColumnInfo(name = "USUREG") val usuReg: String?,
    @ColumnInfo(name = "FECREG") val fecReg: String?,
    @ColumnInfo(name = "FECENV") val fecEnv: String?,
    @ColumnInfo(name = "ESTADOSUP") val estadoSup: String?
)