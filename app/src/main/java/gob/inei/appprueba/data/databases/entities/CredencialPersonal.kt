package gob.inei.appprueba.data.databases.entities

import androidx.room.ColumnInfo

/** Proyección de `PERSONAL` (join con `CARGO`) usada para autenticar usuarios reales. */
data class CredencialPersonal(
    @ColumnInfo(name = "USUARIO") val usuario: String?,
    @ColumnInfo(name = "CLAVE") val clave: String?,
    @ColumnInfo(name = "ACTIVO") val activo: Boolean,
    @ColumnInfo(name = "NOMBRE") val nombre: String,
    @ColumnInfo(name = "APEPATERNO") val apePaterno: String,
    @ColumnInfo(name = "APEMATERNO") val apeMaterno: String,
    @ColumnInfo(name = "NOMCARGO") val nomCargo: String?
)
