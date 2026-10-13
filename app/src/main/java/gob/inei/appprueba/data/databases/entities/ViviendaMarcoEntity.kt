package gob.inei.appprueba.data.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Tabla `CAMPO_VIVIENDA_MARCO` según docs/entities/CONGLOMERADO_MARCO_REEMPLAZO_PERSONA_CAP100.sql. */
@Entity(
    tableName = "CAMPO_VIVIENDA_MARCO",
    foreignKeys = [
        ForeignKey(
            entity = ConglomeradoEntity::class,
            parentColumns = ["CONGLOME"],
            childColumns = ["CONGLOME"],
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["CONGLOME", "VIVIENDA"], unique = true),
        Index("CONGLOME")
    ]
)
data class ViviendaMarcoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID") val id: Long = 0,
    @ColumnInfo(name = "CONGLOME") val conglome: String,
    @ColumnInfo(name = "VIVIENDA") val vivienda: String,
    @ColumnInfo(name = "MANZANA") val manzana: String?,
    @ColumnInfo(name = "NROVIVIENDA") val nroVivienda: String?,
    @ColumnInfo(name = "TIPOVIVIENDA") val tipoVivienda: String?,
    @ColumnInfo(name = "CATVIA") val catVia: String?,
    @ColumnInfo(name = "NOMBREVIA") val nombreVia: String?,
    @ColumnInfo(name = "PUERTA") val puerta: String?,
    @ColumnInfo(name = "INTERIOR") val interior: String?,
    @ColumnInfo(name = "PISO") val piso: String?,
    @ColumnInfo(name = "MZ") val mz: String?,
    @ColumnInfo(name = "LOTE") val lote: String?,
    @ColumnInfo(name = "KM") val km: String?,
    @ColumnInfo(name = "NROBLOCK") val nroBlock: String?,
    @ColumnInfo(name = "CODCCPP") val codCcpp: String?,
    @ColumnInfo(name = "NOMCCPP") val nomCcpp: String?,
    @ColumnInfo(name = "LATORIGEN_GPS") val latOrigenGps: String?,
    @ColumnInfo(name = "LONORIGEN_GPS") val lonOrigenGps: String?,
    @ColumnInfo(name = "USUARIODIG") val usuarioDig: String?,
    @ColumnInfo(name = "FECHADIG") val fechaDig: String?,
    @ColumnInfo(name = "USUARIOMOD") val usuarioMod: String?,
    @ColumnInfo(name = "FECHAMOD") val fechaMod: String?,
    @ColumnInfo(name = "activo") val activo: Int?,
    @ColumnInfo(name = "USUCRE") val usuCre: String?,
    @ColumnInfo(name = "FECCRE") val fecCre: String?,
    @ColumnInfo(name = "USUREG") val usuReg: String?,
    @ColumnInfo(name = "FECREG") val fecReg: String?,
    @ColumnInfo(name = "FECENV") val fecEnv: String?
)
