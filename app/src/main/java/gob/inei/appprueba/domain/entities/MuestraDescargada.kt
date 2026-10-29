package gob.inei.appprueba.domain.entities

import java.io.File

/** ZIP de muestra descargado del servidor: [nombre] visible y [archivo] local (RF-33/RF-35). */
data class MuestraDescargada(
    val nombre: String,
    val archivo: File
)
