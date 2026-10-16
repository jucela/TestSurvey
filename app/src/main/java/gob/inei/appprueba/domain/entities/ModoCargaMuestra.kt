package gob.inei.appprueba.domain.entities

/**
 * Modo con el que se cargó una muestra (RF-27): `MANUAL` al seleccionar archivos
 * ZIP locales y `AUTOMATICO` al sincronizar por FTP. Se persiste en
 * `CARGA_MUESTRA` para que cada tarjeta muestre el modo de su propia última
 * carga de forma independiente por tipo de muestra.
 */
enum class ModoCargaMuestra { MANUAL, AUTOMATICO }
