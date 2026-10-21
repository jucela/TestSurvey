package gob.inei.appprueba.data.databases.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Tabla `PERSONAL` según docs/entities/PERSONAL_PERFIL_CARGO.sql. */
@Entity(
    tableName = "PERSONAL",
    foreignKeys = [
        ForeignKey(
            entity = PerfilEntity::class,
            parentColumns = ["CODPERFIL"],
            childColumns = ["PERFIL"],
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = CargoEntity::class,
            parentColumns = ["CARGO"],
            childColumns = ["CARGO"],
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("PERFIL"), Index("CARGO")]
)
data class PersonalEntity(
    @PrimaryKey
    @ColumnInfo(name = "DNI") val dni: String,
    @ColumnInfo(name = "PERFIL") val perfil: String,
    @ColumnInfo(name = "NOMBRE") val nombre: String,
    @ColumnInfo(name = "APEPATERNO") val apePaterno: String,
    @ColumnInfo(name = "APEMATERNO") val apeMaterno: String,
    @ColumnInfo(name = "ODEI") val odei: String,
    @ColumnInfo(name = "DPTO") val dpto: String?,
    @ColumnInfo(name = "PROV") val prov: String?,
    @ColumnInfo(name = "DIST") val dist: String?,
    @ColumnInfo(name = "CLAVE") val clave: String?,
    @ColumnInfo(name = "USUARIO") val usuario: String?,
    @ColumnInfo(name = "ACTIVO") val activo: Boolean,
    @ColumnInfo(name = "FECHAALTA") val fechaAlta: String?,
    @ColumnInfo(name = "FECHABAJA") val fechaBaja: String?,
    @ColumnInfo(name = "MOVIL") val movil: Boolean?,
    @ColumnInfo(name = "reentrevista") val reentrevista: Boolean?,
    @ColumnInfo(name = "PBASICO") val pbasico: Boolean?,
    @ColumnInfo(name = "PFINAL") val pfinal: Boolean?,
    @ColumnInfo(name = "CARGO") val cargo: String,
    @ColumnInfo(name = "FECHANAC") val fechaNac: String?,
    @ColumnInfo(name = "TELEFONO") val telefono: String?,
    @ColumnInfo(name = "ESTADOCIVIL") val estadoCivil: String?,
    @ColumnInfo(name = "SEXO") val sexo: String?
)
