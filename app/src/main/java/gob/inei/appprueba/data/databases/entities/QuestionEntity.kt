package gob.inei.appprueba.data.databases.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String,
    val anio: Int,
    val capitulo: Int,
    val campoTabla: String,
    val padre: String?,
    val orden: Int?,
    val sortOrder: Int,
    val tipo: String,
    val componenteUi: String,
    val numeracion: String,
    val titulo: String,
    val subtitulo: String
)
