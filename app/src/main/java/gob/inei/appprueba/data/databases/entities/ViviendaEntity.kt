package gob.inei.appprueba.data.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Tabla `CAMPO_VIVIENDA` según docs/entities/VIVIENDA_HOGAR.sql. */
@Entity(
    tableName = "CAMPO_VIVIENDA",
    primaryKeys = ["CONGLOME", "VIVIENDA"],
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
        Index("CONGLOME")
    ]
)
data class ViviendaEntity(
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
    @ColumnInfo(name = "TELEFONO") val telefono: String?,
    @ColumnInfo(name = "ESREEMPLAZO") val esReemplazo: String?,
    @ColumnInfo(name = "NROREEMPLAZO") val nroReemplazo: String?,
    @ColumnInfo(name = "APERTURA") val apertura: String?,
    @ColumnInfo(name = "NROHOGARES") val nroHogares: String?,
    @ColumnInfo(name = "P101") val p101: String?,
    @ColumnInfo(name = "P102A") val p102a: String?,
    @ColumnInfo(name = "P103") val p103: String?,
    @ColumnInfo(name = "P103A") val p103a: String?,
    @ColumnInfo(name = "P104") val p104: Short?,
    @ColumnInfo(name = "P104A") val p104a: Short?,
    @ColumnInfo(name = "P104B1") val p104b1: String?,
    @ColumnInfo(name = "P104B2") val p104b2: String?,
    @ColumnInfo(name = "LATORIGEN_GPS") val latOrigenGps: String?,
    @ColumnInfo(name = "LONORIGEN_GPS") val lonOrigenGps: String?,
    @ColumnInfo(name = "LATITUD_GPS") val latitudGps: String?,
    @ColumnInfo(name = "LONGITUD_GPS") val longitudGps: String?,
    @ColumnInfo(name = "DISTANCIA_GPS") val distanciaGps: String?,
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
    @ColumnInfo(name = "FECENV") val fecEnv: String?
)