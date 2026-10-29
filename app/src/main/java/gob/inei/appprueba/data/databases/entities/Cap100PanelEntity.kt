package gob.inei.appprueba.data.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity

/** Tabla `CAP100PANEL` según docs/entities/CONGLOMERADO_MARCO_REEMPLAZO_PERSONA_CAP100.sql. */
@Entity(
    tableName = "CAP100PANEL",
    primaryKeys = ["ANHO", "MES", "CONGLOME", "VIVIENDA", "HOGAR"]
)
data class Cap100PanelEntity(
    @ColumnInfo(name = "ANHO") val anho: String,
    @ColumnInfo(name = "MES") val mes: String,
    @ColumnInfo(name = "CONGLOME") val conglome: String,
    @ColumnInfo(name = "VIVIENDA") val vivienda: String,
    @ColumnInfo(name = "HOGAR") val hogar: String,
    @ColumnInfo(name = "UBIGEO") val ubigeo: String?,
    @ColumnInfo(name = "DOMINIO") val dominio: String?,
    @ColumnInfo(name = "ESTRATO") val estrato: String?,
    @ColumnInfo(name = "P101") val p101: String?,
    @ColumnInfo(name = "P102A") val p102a: String?,
    @ColumnInfo(name = "P103") val p103: String?,
    @ColumnInfo(name = "P103A") val p103a: String?,
    @ColumnInfo(name = "P104") val p104: String?,
    @ColumnInfo(name = "P104A") val p104a: String?,
    @ColumnInfo(name = "P105A") val p105a: String?,
    @ColumnInfo(name = "P110") val p110: String?,
    @ColumnInfo(name = "P110A") val p110a: String?,
    @ColumnInfo(name = "P111") val p111: String?,
    @ColumnInfo(name = "P112_1") val p112_1: String?,
    @ColumnInfo(name = "P112_2") val p112_2: String?,
    @ColumnInfo(name = "P112_3") val p112_3: String?,
    @ColumnInfo(name = "P112_4") val p112_4: String?,
    @ColumnInfo(name = "P112_5") val p112_5: String?,
    @ColumnInfo(name = "P112_6") val p112_6: String?,
    @ColumnInfo(name = "P112_7") val p112_7: String?,
    @ColumnInfo(name = "P112a") val p112a: String?,
    @ColumnInfo(name = "USUCRE") val usuCre: String?,
    @ColumnInfo(name = "FECCRE") val fecCre: String?,
    @ColumnInfo(name = "USUREG") val usuReg: String?,
    @ColumnInfo(name = "FECREG") val fecReg: String?,
    @ColumnInfo(name = "FECENV") val fecEnv: String?,
    @ColumnInfo(name = "P106A") val p106a: String?,
    @ColumnInfo(name = "P113A") val p113a: String?,
    @ColumnInfo(name = "ALERT") val alert: String?
)
