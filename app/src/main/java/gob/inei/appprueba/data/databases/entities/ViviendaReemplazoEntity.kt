package gob.inei.appprueba.data.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Tabla `CAMPO_VIVIENDA_REEMPLAZO` según docs/entities/CONGLOMERADO_MARCO_REEMPLAZO_PERSONA_CAP100.sql. */
@Entity(
    tableName = "CAMPO_VIVIENDA_REEMPLAZO",
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
    indices = [Index("CONGLOME")]
)
data class ViviendaReemplazoEntity(
    @ColumnInfo(name = "CONGLOME") val conglome: String,
    @ColumnInfo(name = "VIVIENDA") val vivienda: String,
    @ColumnInfo(name = "MZN") val mzn: String?,
    @ColumnInfo(name = "TIPVIA") val tipVia: String?,
    @ColumnInfo(name = "NOMVIA") val nomVia: String?,
    @ColumnInfo(name = "PTANRO") val ptaNro: String?,
    @ColumnInfo(name = "NROBLOCK") val nroBlock: String?,
    @ColumnInfo(name = "NROPISO") val nroPiso: String?,
    @ColumnInfo(name = "INTNRO") val intNro: String?,
    @ColumnInfo(name = "INTALF") val intAlf: String?,
    @ColumnInfo(name = "NROMZA") val nroMza: String?,
    @ColumnInfo(name = "NROLOTE") val nroLote: String?,
    @ColumnInfo(name = "NROKM") val nroKm: String?,
    @ColumnInfo(name = "OCUPADA") val ocupada: String?,
    @ColumnInfo(name = "ORDEN") val orden: String?,
    @ColumnInfo(name = "ORDENVIV") val ordenViv: String?,
    @ColumnInfo(name = "SELECCION") val seleccion: String?,
    @ColumnInfo(name = "CONTIGUASEL") val contiguaSel: String?,
    @ColumnInfo(name = "REEMPLAZO") val reemplazo: String?,
    @ColumnInfo(name = "CONTIGUAREE") val contiguaRee: String?,
    @ColumnInfo(name = "SELECCIONANT") val seleccionAnt: String?,
    @ColumnInfo(name = "ANHOANT") val anhoAnt: String?,
    @ColumnInfo(name = "MESANT") val mesAnt: String?,
    @ColumnInfo(name = "DPTO") val dpto: String?,
    @ColumnInfo(name = "ODEI") val odei: String?,
    @ColumnInfo(name = "USOLOCAL") val usoLocal: String?,
    @ColumnInfo(name = "JEFEHOGAR") val jefeHogar: String?,
    @ColumnInfo(name = "REEMPLAZOCAMPO") val reemplazoCampo: String?,
    @ColumnInfo(name = "GRUPO") val grupo: String?,
    @ColumnInfo(name = "NOMCCPP") val nomCcpp: String?,
    @ColumnInfo(name = "AERINI") val aerIni: String?,
    @ColumnInfo(name = "AERFIN") val aerFin: String?,
    @ColumnInfo(name = "CODCCPP") val codCcpp: String?,
    @ColumnInfo(name = "LATITUD") val latitud: String?,
    @ColumnInfo(name = "LONGITUD") val longitud: String?,
    @ColumnInfo(name = "USUCRE") val usuCre: String?,
    @ColumnInfo(name = "FECCRE") val fecCre: String?,
    @ColumnInfo(name = "USUREG") val usuReg: String?,
    @ColumnInfo(name = "FECREG") val fecReg: String?,
    @ColumnInfo(name = "FECENV") val fecEnv: String?
)
