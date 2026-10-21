package gob.inei.appprueba.data.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Tabla `CAMPO_CONGLOMERADO` según docs/entities/CONGLOMERADO_MARCO_REEMPLAZO_PERSONA_CAP100.sql. */
@Entity(
    tableName = "CAMPO_CONGLOMERADO",
    foreignKeys = [
        ForeignKey(
            entity = OdeiEntity::class,
            parentColumns = ["CODODEI"],
            childColumns = ["ODEI"],
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = PeriodoEntity::class,
            parentColumns = ["CODPER"],
            childColumns = ["PERIODO"],
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["CONGLOME"], unique = true),
        Index("ODEI"),
        Index("PERIODO")
    ]
)
data class ConglomeradoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID") val id: Long = 0,
    @ColumnInfo(name = "CONGLOME") val conglome: String,
    @ColumnInfo(name = "ANHO") val anho: String?,
    @ColumnInfo(name = "MES") val mes: String?,
    @ColumnInfo(name = "PERIODO") val periodo: String?,
    @ColumnInfo(name = "ESTRATO") val estrato: String?,
    @ColumnInfo(name = "DPTO") val dpto: String?,
    @ColumnInfo(name = "PROV") val prov: String?,
    @ColumnInfo(name = "DIST") val dist: String?,
    @ColumnInfo(name = "ZONA") val zona: String?,
    @ColumnInfo(name = "DOMINIO") val dominio: String?,
    @ColumnInfo(name = "TIPOSELECCION") val tipoSeleccion: String?,
    @ColumnInfo(name = "ODEI") val odei: String?,
    @ColumnInfo(name = "AERINI") val aerIni: String?,
    @ColumnInfo(name = "AERFIN") val aerFin: String?,
    @ColumnInfo(name = "MUESTRA") val muestra: String?,
    @ColumnInfo(name = "SEDE") val sede: String?,
    @ColumnInfo(name = "PRUEBA") val prueba: Boolean?,
    @ColumnInfo(name = "COVID19") val covid19: Int?,
    @ColumnInfo(name = "USUARIODIG") val usuarioDig: String?,
    @ColumnInfo(name = "FECHADIG") val fechaDig: String?,
    @ColumnInfo(name = "USUARIOMOD") val usuarioMod: String?,
    @ColumnInfo(name = "FECHAMOD") val fechaMod: String?,
    @ColumnInfo(name = "USUCRE") val usuCre: String?,
    @ColumnInfo(name = "FECCRE") val fecCre: String?,
    @ColumnInfo(name = "USUREG") val usuReg: String?,
    @ColumnInfo(name = "FECREG") val fecReg: String?,
    @ColumnInfo(name = "FECENV") val fecEnv: String?
)
