package gob.inei.appprueba.data.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Registro de la fecha/hora de la última carga por tipo (RF-26). Para `PERSONAL`
 * se registra una fila; para `VIVIENDAS`, una fila por combinación `(anho,odei,mes)`.
 */
@Entity(
    tableName = "CARGA_MUESTRA",
    indices = [Index("tipo", "anho", "odei", "mes")]
)
data class CargaMuestraEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "tipo") val tipo: String,
    @ColumnInfo(name = "anho") val anho: String?,
    @ColumnInfo(name = "odei") val odei: String?,
    @ColumnInfo(name = "mes") val mes: String?,
    @ColumnInfo(name = "fechaHora") val fechaHora: Long
)
