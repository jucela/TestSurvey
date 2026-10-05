package gob.inei.appprueba.data.databases.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "answers")
data class AnswerEntity(
    @PrimaryKey val preguntaId: String,
    val valor: String,
    val textoLibre: String?,
    val actualizado: Long
)
