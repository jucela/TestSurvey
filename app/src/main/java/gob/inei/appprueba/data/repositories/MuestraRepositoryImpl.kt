package gob.inei.appprueba.data.repositories

import android.content.Context
import androidx.core.net.toUri
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import gob.inei.appprueba.data.databases.EncuestaDatabase
import gob.inei.appprueba.data.databases.dao.Cap100PanelDao
import gob.inei.appprueba.data.databases.dao.CargaMuestraDao
import gob.inei.appprueba.data.databases.dao.CargoDao
import gob.inei.appprueba.data.databases.dao.ConglomeradoDao
import gob.inei.appprueba.data.databases.dao.OdeiDao
import gob.inei.appprueba.data.databases.dao.PerfilDao
import gob.inei.appprueba.data.databases.dao.PersonaPanelDao
import gob.inei.appprueba.data.databases.dao.PersonalDao
import gob.inei.appprueba.data.databases.dao.ViviendaMarcoDao
import gob.inei.appprueba.data.databases.dao.ViviendaReemplazoDao
import gob.inei.appprueba.data.databases.entities.CargaMuestraEntity
import gob.inei.appprueba.data.databases.entities.CargoEntity
import gob.inei.appprueba.data.databases.entities.PerfilEntity
import gob.inei.appprueba.data.databases.entities.PersonalEntity
import gob.inei.appprueba.data.databases.toOdeiMeses
import gob.inei.appprueba.data.sources.AnioAplicativoProvider
import gob.inei.appprueba.data.sources.MuestraViviendas
import gob.inei.appprueba.data.sources.MuestraZipReader
import gob.inei.appprueba.data.sources.NombreArchivoResolver
import gob.inei.appprueba.data.sources.PasswordMuestraProvider
import gob.inei.appprueba.data.sources.PersonalXmlParser
import gob.inei.appprueba.data.sources.ViviendaXmlParser
import gob.inei.appprueba.domain.entities.Cargo
import gob.inei.appprueba.domain.entities.EstadoArchivo
import gob.inei.appprueba.domain.entities.EstadoMuestraPersonal
import gob.inei.appprueba.domain.entities.EstadoMuestraViviendas
import gob.inei.appprueba.domain.entities.MuestraException
import gob.inei.appprueba.domain.entities.MuestraNombre
import gob.inei.appprueba.domain.entities.Perfil
import gob.inei.appprueba.domain.entities.Personal
import gob.inei.appprueba.domain.entities.ResultadoArchivo
import gob.inei.appprueba.domain.entities.TipoError
import gob.inei.appprueba.domain.entities.TipoMuestra
import gob.inei.appprueba.domain.naming.MuestraNombreParser
import gob.inei.appprueba.domain.repositories.MuestraRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

/**
 * Implementación de la carga de muestras (RF-03..RF-27): descarga el ZIP,
 * detecta el tipo por nombre, valida año/ODEI/mes contra el contenido y carga
 * de forma transaccional según el tipo. Cada archivo se procesa de forma
 * independiente (RF-25) y su resultado se devuelve como [ResultadoArchivo].
 */
@Singleton
class MuestraRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: EncuestaDatabase,
    private val personalDao: PersonalDao,
    private val cargoDao: CargoDao,
    private val perfilDao: PerfilDao,
    private val odeiDao: OdeiDao,
    private val conglomeradoDao: ConglomeradoDao,
    private val viviendaMarcoDao: ViviendaMarcoDao,
    private val viviendaReemplazoDao: ViviendaReemplazoDao,
    private val personaPanelDao: PersonaPanelDao,
    private val cap100PanelDao: Cap100PanelDao,
    private val cargaMuestraDao: CargaMuestraDao,
    private val zipReader: MuestraZipReader,
    private val personalParser: PersonalXmlParser,
    private val viviendaParser: ViviendaXmlParser,
    private val nombreParser: MuestraNombreParser,
    private val nombreResolver: NombreArchivoResolver,
    private val passwordProvider: PasswordMuestraProvider,
    private val anioProvider: AnioAplicativoProvider
) : MuestraRepository {

    override suspend fun cargar(uriZip: String): ResultadoArchivo = withContext(Dispatchers.IO) {
        val nombre = nombreResolver.resolver(uriZip) ?: nombreRespaldo(uriZip)
        val nombreMuestra = try {
            nombreParser.parse(nombre, anioProvider.obtener())
        } catch (e: MuestraException) {
            return@withContext ResultadoArchivo(
                nombre = nombre,
                estado = e.tipo.estadoDeFallo(),
                tipoError = e.tipo,
                detalle = e.detalle
            )
        } catch (e: Exception) {
            return@withContext ResultadoArchivo(
                nombre = nombre,
                estado = EstadoArchivo.ERROR,
                tipoError = TipoError.ERROR_ESCRITURA,
                detalle = "No se pudo identificar el archivo"
            )
        }

        try {
            when (nombreMuestra.tipo) {
                TipoMuestra.PERSONAL -> cargarPersonal(uriZip, nombre, nombreMuestra)
                TipoMuestra.VIVIENDAS -> cargarViviendas(uriZip, nombre, nombreMuestra)
            }
        } catch (e: MuestraException) {
            ResultadoArchivo(nombre, e.tipo.estadoDeFallo(), nombreMuestra.tipo, e.tipo, e.detalle)
        } catch (e: Exception) {
            ResultadoArchivo(
                nombre,
                EstadoArchivo.ERROR,
                nombreMuestra.tipo,
                TipoError.ERROR_ESCRITURA,
                "No se pudo completar la carga"
            )
        }
    }

    override suspend fun nombresDe(urisZip: List<String>): List<String> = withContext(Dispatchers.IO) {
        urisZip.map { nombreResolver.resolver(it) ?: nombreRespaldo(it) }
    }

    override fun observarMuestraPersonal(): Flow<EstadoMuestraPersonal> =
        combine(
            personalDao.observarCount(),
            cargaMuestraDao.observarUltimaFecha(TipoMuestra.PERSONAL.name)
        ) { total, fecha ->
            EstadoMuestraPersonal(cargado = total > 0, fechaCargaMs = fecha)
        }

    override fun observarMuestraViviendas(): Flow<EstadoMuestraViviendas> =
        combine(
            conglomeradoDao.observarOdeiMeses(),
            cargaMuestraDao.observarUltimaFecha(TipoMuestra.VIVIENDAS.name)
        ) { filas, fecha ->
            val odeis = filas.toOdeiMeses()
            EstadoMuestraViviendas(
                cargado = odeis.isNotEmpty(),
                fechaCargaMs = fecha,
                odeis = odeis
            )
        }

    private suspend fun cargarPersonal(
        uriZip: String,
        nombre: String,
        nombreMuestra: MuestraNombre
    ): ResultadoArchivo {
        val password = passwordProvider.obtener()
        if (password.isEmpty()) {
            throw MuestraException(
                TipoError.CONFIGURACION,
                "La contraseña interna de la muestra no está configurada"
            )
        }
        val dir = dirTemporal()
        val zip = copiarTemporal(uriZip)
        var contenido: File? = null
        try {
            contenido = zipReader.extraerContenido(zip, password, MuestraZipReader.EXTENSION_XML, dir)
            val personal = contenido.inputStream().use { personalParser.parse(it, nombreMuestra.anio) }
            validarReferenciasPersonal(personal)
            database.withTransaction {
                personalDao.deleteAll()
                cargoDao.insertAll(CARGOS.map { it.toEntity() })
                perfilDao.insertAll(PERFILES.map { it.toEntity() })
                personalDao.insertAll(personal.map { it.toEntity() })
                cargaMuestraDao.deletePorTipo(TipoMuestra.PERSONAL.name)
                cargaMuestraDao.insert(
                    CargaMuestraEntity(
                        tipo = TipoMuestra.PERSONAL.name,
                        anho = null,
                        odei = null,
                        mes = null,
                        fechaHora = ahora()
                    )
                )
            }
            return ResultadoArchivo(nombre, EstadoArchivo.CARGADO, TipoMuestra.PERSONAL)
        } finally {
            limpiar(zip, contenido, dir)
        }
    }

    private suspend fun cargarViviendas(
        uriZip: String,
        nombre: String,
        nombreMuestra: MuestraNombre
    ): ResultadoArchivo {
        val dir = dirTemporal()
        val zip = copiarTemporal(uriZip)
        var contenido: File? = null
        try {
            contenido = zipReader.extraerContenido(zip, null, MuestraZipReader.EXTENSION_CFG, dir)
            val muestra = contenido.inputStream().use { viviendaParser.parse(it) }
            validarContenidoViviendas(nombreMuestra, muestra)
            if (odeiDao.existe(muestra.odei) == 0) {
                throw MuestraException(
                    TipoError.ODEI_INVALIDO,
                    "El ODEI de la muestra no existe en la tabla ODEI"
                )
            }
            val existia = conglomeradoDao.contarCombinacion(muestra.anio, muestra.mes, muestra.odei) > 0
            database.withTransaction {
                personaPanelDao.deleteCombinacion(muestra.anio, muestra.mes, muestra.odei)
                cap100PanelDao.deleteCombinacion(muestra.anio, muestra.mes, muestra.odei)
                viviendaReemplazoDao.deleteCombinacion(muestra.anio, muestra.mes, muestra.odei)
                viviendaMarcoDao.deleteCombinacion(muestra.anio, muestra.mes, muestra.odei)
                conglomeradoDao.deleteCombinacion(muestra.anio, muestra.mes, muestra.odei)
                conglomeradoDao.insertAll(muestra.conglomerados)
                viviendaMarcoDao.insertAll(muestra.viviendas)
                viviendaReemplazoDao.insertAll(muestra.reemplazos)
                personaPanelDao.insertAll(muestra.personas)
                cap100PanelDao.insertAll(muestra.cap100)
                cargaMuestraDao.deleteCombinacion(
                    TipoMuestra.VIVIENDAS.name,
                    muestra.anio,
                    muestra.odei,
                    muestra.mes
                )
                cargaMuestraDao.insert(
                    CargaMuestraEntity(
                        tipo = TipoMuestra.VIVIENDAS.name,
                        anho = muestra.anio,
                        odei = muestra.odei,
                        mes = muestra.mes,
                        fechaHora = ahora()
                    )
                )
            }
            val estado = if (existia) EstadoArchivo.REEMPLAZADO else EstadoArchivo.CARGADO
            return ResultadoArchivo(nombre, estado, TipoMuestra.VIVIENDAS)
        } finally {
            limpiar(zip, contenido, dir)
        }
    }

    private suspend fun validarReferenciasPersonal(personal: List<Personal>) {
        val perfilesValidos = PERFILES.map { it.codPerfil }.toHashSet()
        val cargosValidos = CARGOS.map { it.cargo }.toHashSet()
        personal.forEach { registro ->
            if (registro.perfil !in perfilesValidos) {
                throw MuestraException(TipoError.DATOS_INVALIDOS, "PERFIL inexistente en la muestra")
            }
            if (registro.cargo !in cargosValidos) {
                throw MuestraException(TipoError.DATOS_INVALIDOS, "CARGO inexistente en la muestra")
            }
            if (odeiDao.existe(registro.odei) == 0) {
                throw MuestraException(
                    TipoError.ODEI_INVALIDO,
                    "El ODEI de un registro de personal no existe"
                )
            }
        }
    }

    private fun validarContenidoViviendas(nombre: MuestraNombre, muestra: MuestraViviendas) {
        val anio = nombre.anio.toString()
        if (muestra.anioRaiz != anio || muestra.anio != anio) {
            throw discrepancia("El año del contenido no coincide con el nombre")
        }
        if (nombre.odei != muestra.odei) {
            throw discrepancia("El ODEI del contenido no coincide con el nombre")
        }
        if (nombre.mes != muestra.mes) {
            throw discrepancia("El mes del contenido no coincide con el nombre")
        }
    }

    private fun discrepancia(detalle: String) =
        MuestraException(TipoError.DISCREPANCIA_NOMBRE_CONTENIDO, detalle)

    private fun dirTemporal(): File = File(context.cacheDir, DIR_TEMPORAL)

    private fun copiarTemporal(uriZip: String): File {
        val destino = File(context.cacheDir, ZIP_TEMPORAL)
        val entrada = try {
            context.contentResolver.openInputStream(uriZip.toUri())
        } catch (e: Exception) {
            throw MuestraException(TipoError.ARCHIVO_INVALIDO, "No se pudo abrir el archivo", e)
        } ?: throw MuestraException(TipoError.ARCHIVO_INVALIDO, "No se pudo abrir el archivo")

        try {
            entrada.use { origen ->
                destino.outputStream().use { salida -> origen.copyTo(salida) }
            }
        } catch (e: Exception) {
            destino.delete()
            throw MuestraException(TipoError.ARCHIVO_INVALIDO, "No se pudo leer el archivo", e)
        }
        return destino
    }

    private fun limpiar(zip: File?, contenido: File?, dir: File) {
        zip?.delete()
        contenido?.delete()
        dir.delete()
    }

    private fun nombreRespaldo(uriZip: String): String =
        uriZip.substringAfterLast('/').ifBlank { uriZip }

    private fun ahora(): Long = System.currentTimeMillis()

    private fun TipoError.estadoDeFallo(): EstadoArchivo = when (this) {
        TipoError.CONTRASENA_INVALIDA,
        TipoError.CIFRADO_NO_SOPORTADO,
        TipoError.ERROR_ESCRITURA,
        TipoError.CONFIGURACION -> EstadoArchivo.ERROR
        else -> EstadoArchivo.RECHAZADO
    }

    private fun Cargo.toEntity() = CargoEntity(cargo = cargo, nomCargo = nomCargo, abrCargo = abrCargo)

    private fun Perfil.toEntity() = PerfilEntity(codPerfil = codPerfil, nomPerfil = nomPerfil)

    private fun Personal.toEntity() = PersonalEntity(
        dni = dni,
        perfil = perfil,
        nombre = nombre,
        apePaterno = apePaterno,
        apeMaterno = apeMaterno,
        odei = odei,
        dpto = dpto,
        prov = prov,
        dist = dist,
        clave = clave,
        usuario = usuario,
        activo = activo,
        fechaAlta = fechaAlta,
        fechaBaja = fechaBaja,
        movil = movil,
        reentrevista = reentrevista,
        pbasico = pbasico,
        pfinal = pfinal,
        cargo = cargo,
        fechaNac = fechaNac,
        telefono = telefono,
        estadoCivil = estadoCivil,
        sexo = sexo
    )

    companion object {
        private const val DIR_TEMPORAL = "muestra"
        private const val ZIP_TEMPORAL = "muestra_entrada.zip"

        /**
         * Semillas de CARGO según docs/entities/PERSONAL_PERFIL_CARGO.sql más
         * los códigos faltantes aprobados (DP-3).
         */
        val CARGOS = listOf(
            Cargo("01", "Supervisor(a) Nacional de Encuestas", "SUPN"),
            Cargo("02", "Coordinador(a) Departamental de Proyectos", "COORD"),
            Cargo("03", "Supervisor(a) Local de Operadores de Aplicador de Encuestas", "SUP"),
            Cargo("04", "Operador(a) de aplicación de encuestas (Encuestador(a))", "ENC"),
            Cargo("05", "Asistente de soporte técnico en encuesta", null),
            Cargo("06", "ANALISTA DE CONSISTENCIA", null),
            Cargo("07", "DIGITADOR(A)", null),
            Cargo("08", "Analista Programador de Sistemas", null),
            Cargo("09", "Metodólogo(a)", null),
            Cargo("10", "Jefe(a) Operación de Campo", null),
            Cargo("11", "ADMINISTRATIVO(A)", null),
            Cargo("12", "ANALISTA SENIOR DE CONSISTENCIA", null),
            Cargo("13", "JEFE(A) DEL PROYECTO", null),
            Cargo("19", "Jefe(a) de Monitoreo", null),
            Cargo("20", "Analista de Monitoreo", null),
            Cargo("22", "Asistente Informático", null),
            Cargo("23", "ASISTENTE DE CONSISTENCIA", null),
            Cargo("99", "CARGO NO ESPECIFICADO", null)
        )

        /**
         * Semillas de PERFIL según el SQL más los códigos faltantes aprobados (DP-3).
         */
        val PERFILES = listOf(
            Perfil("01", "OPERADOR DE CAMPO"),
            Perfil("02", "OPERADOR DEL SISTEMA"),
            Perfil("03", "ADMINISTRADOR DE CAMPO"),
            Perfil("04", "ADMINISTRADOR DEL SISTEMA"),
            Perfil("05", "PROGRAMADOR DEL TRABAJO DE CAMPO"),
            Perfil("06", "SUPERVISOR DE CAMPO"),
            Perfil("08", "ASISTENTE DE CAMPO 1"),
            Perfil("11", "ASISTENTE DE CAMPO 2"),
            Perfil("99", "USUARIO NO IDENTIFICADO")
        )
    }
}
