package gob.inei.appprueba.domain.entities

/** Registro de personal de la muestra (tabla PERSONAL). */
data class Personal(
    val dni: String,
    val perfil: String,
    val nombre: String,
    val apePaterno: String,
    val apeMaterno: String,
    val odei: String,
    val dpto: String?,
    val prov: String?,
    val dist: String?,
    val clave: String?,
    val usuario: String?,
    val activo: Boolean,
    val fechaAlta: String?,
    val fechaBaja: String?,
    val movil: Boolean?,
    val reentrevista: Boolean?,
    val pbasico: Boolean?,
    val pfinal: Boolean?,
    val cargo: String,
    val fechaNac: String?,
    val telefono: String?,
    val estadoCivil: String?,
    val sexo: String?
)
