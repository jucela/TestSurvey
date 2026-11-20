package gob.inei.appprueba.data.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Tabla `PERSONA_PANEL` según docs/entities/CONGLOMERADO_MARCO_REEMPLAZO_PERSONA_CAP100.sql. */
@Entity(
    tableName = "PERSONA_PANEL",
    primaryKeys = ["CONGLOME", "VIVIENDA", "CODPANEL"],
    foreignKeys = [
        ForeignKey(
            entity = ViviendaMarcoEntity::class,
            parentColumns = ["CONGLOME", "VIVIENDA"],
            childColumns = ["CONGLOME", "VIVIENDA"],
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("CONGLOME", "VIVIENDA")]
)
data class PersonaPanelEntity(
    @ColumnInfo(name = "CONGLOME") val conglome: String,
    @ColumnInfo(name = "VIVIENDA") val vivienda: String,
    @ColumnInfo(name = "HOGAR") val hogar: String?,
    @ColumnInfo(name = "CODPANEL") val codPanel: String,
    @ColumnInfo(name = "P201") val p201: String?,
    @ColumnInfo(name = "P202A") val p202a: String?,
    @ColumnInfo(name = "P202B") val p202b: String?,
    @ColumnInfo(name = "P202C") val p202c: String?,
    @ColumnInfo(name = "P203") val p203: String?,
    @ColumnInfo(name = "P207") val p207: String?,
    @ColumnInfo(name = "P208A") val p208a: String?,
    @ColumnInfo(name = "P301A") val p301a: String?,
    @ColumnInfo(name = "P301B") val p301b: String?,
    @ColumnInfo(name = "P401") val p401: String?,
    @ColumnInfo(name = "P558C") val p558c: String?,
    @ColumnInfo(name = "P558C_ESPECIFIQUE") val p558cEspecifique: String?,
    @ColumnInfo(name = "USUCRE") val usuCre: String?,
    @ColumnInfo(name = "FECCRE") val fecCre: String?,
    @ColumnInfo(name = "USUREG") val usuReg: String?,
    @ColumnInfo(name = "FECREG") val fecReg: String?,
    @ColumnInfo(name = "FECENV") val fecEnv: String?,
    @ColumnInfo(name = "P204") val p204: String?,
    @ColumnInfo(name = "P401H1") val p401h1: String?,
    @ColumnInfo(name = "P401H2") val p401h2: String?,
    @ColumnInfo(name = "P401H3") val p401h3: String?,
    @ColumnInfo(name = "P401H4") val p401h4: String?,
    @ColumnInfo(name = "P401H5") val p401h5: String?,
    @ColumnInfo(name = "P401H6") val p401h6: String?,
    @ColumnInfo(name = "P558A1") val p558a1: String?,
    @ColumnInfo(name = "P558A2") val p558a2: String?,
    @ColumnInfo(name = "P558A3") val p558a3: String?,
    @ColumnInfo(name = "P558A4") val p558a4: String?,
    @ColumnInfo(name = "P558A5") val p558a5: String?,
    @ColumnInfo(name = "P558A6") val p558a6: String?,
    @ColumnInfo(name = "P401H11") val p401h11: String?,
    @ColumnInfo(name = "P401H12") val p401h12: String?,
    @ColumnInfo(name = "P401H13") val p401h13: String?,
    @ColumnInfo(name = "P401H14") val p401h14: String?,
    @ColumnInfo(name = "P401H15") val p401h15: String?,
    @ColumnInfo(name = "P401H16") val p401h16: String?,
    @ColumnInfo(name = "P401H17") val p401h17: String?,
    @ColumnInfo(name = "P558C1") val p558c1: String?,
    @ColumnInfo(name = "P558C1_ESPECIFIQUE") val p558c1Especifique: String?
)
