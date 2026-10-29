package gob.inei.appprueba.data.databases.dao

import androidx.room.Dao
import androidx.room.Query
import gob.inei.appprueba.data.databases.projections.ConglomeradoMarcoRow
import gob.inei.appprueba.data.databases.projections.DetalleMarcoRow
import gob.inei.appprueba.data.databases.projections.MarcoFilaRow
import kotlinx.coroutines.flow.Flow

@Dao
interface MarcoDao {

    @Query(
        """
        SELECT
            m.CONGLOME AS conglome,
            m.VIVIENDA AS vivienda,
            COALESCE(NULLIF(TRIM(v.NROVIVIENDA), ''), m.NROVIVIENDA) AS nroVivienda,
            m.activo AS activo,
            h.HOGAR AS hogar,
            m.TIPOVIVIENDA AS seleccion,
            v.NROREEMPLAZO AS nroReemplazo,
            h.RESFIN AS resfin,
            v.ESREEMPLAZO AS esReemplazo
        FROM CAMPO_VIVIENDA_MARCO m
        INNER JOIN CAMPO_CONGLOMERADO c ON c.CONGLOME = m.CONGLOME
        LEFT JOIN CAMPO_VIVIENDA v
            ON v.CONGLOME = m.CONGLOME AND v.VIVIENDA = m.VIVIENDA
        LEFT JOIN CAMPO_HOGAR h
            ON h.CONGLOME = m.CONGLOME AND h.VIVIENDA = m.VIVIENDA
        WHERE (:soloActivos = 0 OR m.activo = 1)
          AND (:patron IS NULL OR LOWER(m.CONGLOME) LIKE :patron ESCAPE '\')
          AND (:odei IS NULL OR c.ODEI = :odei)
          AND (:mes IS NULL OR c.MES = :mes)
          AND (:periodo IS NULL OR c.PERIODO = :periodo)
          AND (:conglomerado IS NULL OR m.CONGLOME = :conglomerado)
        ORDER BY m.CONGLOME, m.VIVIENDA, h.HOGAR
        """
    )
    fun observarFilas(
        soloActivos: Int,
        patron: String?,
        odei: String?,
        mes: String?,
        periodo: String?,
        conglomerado: String?
    ): Flow<List<MarcoFilaRow>>

    @Query(
        """
        SELECT DISTINCT c.CONGLOME AS conglome,
               c.ESTRATO AS estrato,
               c.TIPOSELECCION AS tipoSeleccion
        FROM CAMPO_CONGLOMERADO c
        WHERE (:odei IS NULL OR c.ODEI = :odei)
          AND (:mes IS NULL OR c.MES = :mes)
          AND (:periodo IS NULL OR c.PERIODO = :periodo)
        ORDER BY c.CONGLOME
        """
    )
    fun observarConglomerados(
        odei: String?,
        mes: String?,
        periodo: String?
    ): Flow<List<ConglomeradoMarcoRow>>

    @Query(
        """
        SELECT c.CONGLOME AS conglome,
               c.ESTRATO AS estrato,
               c.TIPOSELECCION AS tipoSeleccion
        FROM CAMPO_CONGLOMERADO c
        WHERE c.CONGLOME = :conglome
        LIMIT 1
        """
    )
    fun observarConglomerado(conglome: String): Flow<ConglomeradoMarcoRow?>

    @Query("SELECT COUNT(*) FROM CAMPO_CONGLOMERADO")
    fun observarTotalConglomerados(): Flow<Int>

    @Query(
        """
        SELECT
            m.NOMBREVIA AS marcoNombreVia,
            m.PUERTA AS marcoPuerta,
            m.INTERIOR AS marcoInterior,
            m.PISO AS marcoPiso,
            m.MZ AS marcoMz,
            m.LOTE AS marcoLote,
            v.NOMBREVIA AS viviendaNombreVia,
            v.PUERTA AS viviendaPuerta,
            v.INTERIOR AS viviendaInterior,
            v.PISO AS viviendaPiso,
            v.MZ AS viviendaMz,
            v.LOTE AS viviendaLote
        FROM CAMPO_VIVIENDA_MARCO m
        LEFT JOIN CAMPO_VIVIENDA v
            ON v.CONGLOME = m.CONGLOME AND v.VIVIENDA = m.VIVIENDA
        WHERE m.CONGLOME = :conglome AND m.VIVIENDA = :vivienda
        LIMIT 1
        """
    )
    suspend fun obtenerDetalle(conglome: String, vivienda: String): DetalleMarcoRow?
}