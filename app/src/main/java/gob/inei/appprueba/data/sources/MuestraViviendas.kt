package gob.inei.appprueba.data.sources

import gob.inei.appprueba.data.databases.entities.Cap100PanelEntity
import gob.inei.appprueba.data.databases.entities.ConglomeradoEntity
import gob.inei.appprueba.data.databases.entities.PersonaPanelEntity
import gob.inei.appprueba.data.databases.entities.ViviendaMarcoEntity
import gob.inei.appprueba.data.databases.entities.ViviendaReemplazoEntity

/**
 * Contenido ya validado de un `.cfg` de viviendas (RF-19), listo para insertarse
 * en la transacción de la combinación [anio] + [odei] + [mes]. [anioRaiz] es el
 * año leído de la raíz `ENAHO{año}`; [anio]/[odei]/[mes] provienen de
 * `CAMPO_CONGLOMERADO` y se contrastan con el nombre en el repositorio (RF-17).
 */
data class MuestraViviendas(
    val anioRaiz: String,
    val anio: String,
    val odei: String,
    val mes: String,
    val conglomerados: List<ConglomeradoEntity>,
    val viviendas: List<ViviendaMarcoEntity>,
    val reemplazos: List<ViviendaReemplazoEntity>,
    val personas: List<PersonaPanelEntity>,
    val cap100: List<Cap100PanelEntity>
)
