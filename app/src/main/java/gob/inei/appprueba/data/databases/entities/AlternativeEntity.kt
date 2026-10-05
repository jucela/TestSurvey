package gob.inei.appprueba.data.databases.entities

import androidx.room.Entity

@Entity(tableName = "alternatives", primaryKeys = ["pregunta", "orden"])
data class AlternativeEntity(
    val anio: Int,
    val pregunta: String,
    val campoTabla: String,
    val orden: Int,
    val numeracion: String,
    val texto: String,
    val tipoOpcion: String,
    val campoDependiente: String?
)
