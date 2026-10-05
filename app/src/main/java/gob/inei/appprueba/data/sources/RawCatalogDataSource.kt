package gob.inei.appprueba.data.sources

interface RawCatalogDataSource {
    fun catalogo(): String
    fun alternativas(): String
    fun flujo(): String
}
