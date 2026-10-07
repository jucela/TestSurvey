package gob.inei.appprueba.data.databases.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val usuario: String,
    val hash: String,
    val sal: String,
    val iteraciones: Int,
    val creadoEn: Long
)
