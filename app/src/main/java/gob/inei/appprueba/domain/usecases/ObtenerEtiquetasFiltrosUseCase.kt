package gob.inei.appprueba.domain.usecases

import gob.inei.appprueba.domain.entities.CatalogoMarco
import gob.inei.appprueba.domain.entities.EtiquetasFiltrosMarco
import gob.inei.appprueba.domain.entities.MarcoFiltros
import gob.inei.appprueba.domain.entities.OpcionConglomerado
import gob.inei.appprueba.domain.entities.ReglasMarco
import javax.inject.Inject

class ObtenerEtiquetasFiltrosUseCase @Inject constructor() {
    operator fun invoke(
        filtros: MarcoFiltros,
        catalogo: CatalogoMarco,
        conglomerados: List<OpcionConglomerado>
    ): EtiquetasFiltrosMarco {
        val odei = catalogo.odeis.firstOrNull { it.codigo == filtros.odei }
        val mes = catalogo.meses.firstOrNull { it.codigo == filtros.mes }
        val periodo = catalogo.periodos.firstOrNull { it.codigo == filtros.periodo }
        val conglomerado = conglomerados.firstOrNull { it.conglome == filtros.conglomerado }
        return EtiquetasFiltrosMarco(
            odei = odei?.let { ReglasMarco.etiquetaOdei(it.codigo, it.nombre) },
            mes = mes?.let { ReglasMarco.etiquetaMes(it.codigo, it.nombre) },
            periodo = periodo?.let { ReglasMarco.etiquetaPeriodo(it.nombre) },
            conglomerado = conglomerado?.etiqueta,
            estrato = conglomerado?.estrato?.trim()?.takeIf { it.isNotEmpty() },
            tipo = conglomerado?.tipoSeleccion?.trim()?.takeIf { it.isNotEmpty() }
        )
    }
}