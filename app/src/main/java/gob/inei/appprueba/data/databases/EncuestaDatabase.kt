package gob.inei.appprueba.data.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import gob.inei.appprueba.data.databases.dao.AlternativeDao
import gob.inei.appprueba.data.databases.dao.AnswerDao
import gob.inei.appprueba.data.databases.dao.Cap100PanelDao
import gob.inei.appprueba.data.databases.dao.CargaMuestraDao
import gob.inei.appprueba.data.databases.dao.CargoDao
import gob.inei.appprueba.data.databases.dao.ConglomeradoDao
import gob.inei.appprueba.data.databases.dao.DistritoDao
import gob.inei.appprueba.data.databases.dao.DptoDao
import gob.inei.appprueba.data.databases.dao.FlowRuleDao
import gob.inei.appprueba.data.databases.dao.MesDao
import gob.inei.appprueba.data.databases.dao.OdeiDao
import gob.inei.appprueba.data.databases.dao.PaisDao
import gob.inei.appprueba.data.databases.dao.PerfilDao
import gob.inei.appprueba.data.databases.dao.PeriodoDao
import gob.inei.appprueba.data.databases.dao.PersonaPanelDao
import gob.inei.appprueba.data.databases.dao.PersonalDao
import gob.inei.appprueba.data.databases.dao.ProvDao
import gob.inei.appprueba.data.databases.dao.QuestionDao
import gob.inei.appprueba.data.databases.dao.ViviendaMarcoDao
import gob.inei.appprueba.data.databases.dao.ViviendaReemplazoDao
import gob.inei.appprueba.data.databases.entities.AlternativeEntity
import gob.inei.appprueba.data.databases.entities.AnswerEntity
import gob.inei.appprueba.data.databases.entities.Cap100PanelEntity
import gob.inei.appprueba.data.databases.entities.CargaMuestraEntity
import gob.inei.appprueba.data.databases.entities.CargoEntity
import gob.inei.appprueba.data.databases.entities.ConglomeradoEntity
import gob.inei.appprueba.data.databases.entities.DistritoEntity
import gob.inei.appprueba.data.databases.entities.DptoEntity
import gob.inei.appprueba.data.databases.entities.FlowRuleEntity
import gob.inei.appprueba.data.databases.entities.MesEntity
import gob.inei.appprueba.data.databases.entities.OdeiEntity
import gob.inei.appprueba.data.databases.entities.PaisEntity
import gob.inei.appprueba.data.databases.entities.PerfilEntity
import gob.inei.appprueba.data.databases.entities.PeriodoEntity
import gob.inei.appprueba.data.databases.entities.PersonaPanelEntity
import gob.inei.appprueba.data.databases.entities.PersonalEntity
import gob.inei.appprueba.data.databases.entities.ProvEntity
import gob.inei.appprueba.data.databases.entities.QuestionEntity
import gob.inei.appprueba.data.databases.entities.ViviendaMarcoEntity
import gob.inei.appprueba.data.databases.entities.ViviendaReemplazoEntity
import gob.inei.appprueba.data.sources.MaestrasSeeder

@Database(
    entities = [
        QuestionEntity::class,
        AlternativeEntity::class,
        FlowRuleEntity::class,
        AnswerEntity::class,
        PersonalEntity::class,
        CargoEntity::class,
        PerfilEntity::class,
        DptoEntity::class,
        ProvEntity::class,
        DistritoEntity::class,
        OdeiEntity::class,
        PaisEntity::class,
        PeriodoEntity::class,
        ConglomeradoEntity::class,
        ViviendaMarcoEntity::class,
        ViviendaReemplazoEntity::class,
        PersonaPanelEntity::class,
        Cap100PanelEntity::class,
        CargaMuestraEntity::class,
        MesEntity::class
    ],
    version = 6,
    exportSchema = true
)
abstract class EncuestaDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun alternativeDao(): AlternativeDao
    abstract fun flowRuleDao(): FlowRuleDao
    abstract fun answerDao(): AnswerDao
    abstract fun personalDao(): PersonalDao
    abstract fun cargoDao(): CargoDao
    abstract fun perfilDao(): PerfilDao
    abstract fun dptoDao(): DptoDao
    abstract fun provDao(): ProvDao
    abstract fun distritoDao(): DistritoDao
    abstract fun odeiDao(): OdeiDao
    abstract fun paisDao(): PaisDao
    abstract fun periodoDao(): PeriodoDao
    abstract fun conglomeradoDao(): ConglomeradoDao
    abstract fun viviendaMarcoDao(): ViviendaMarcoDao
    abstract fun viviendaReemplazoDao(): ViviendaReemplazoDao
    abstract fun personaPanelDao(): PersonaPanelDao
    abstract fun cap100PanelDao(): Cap100PanelDao
    abstract fun cargaMuestraDao(): CargaMuestraDao
    abstract fun mesDao(): MesDao

    companion object {
        /** Solo crea la tabla de usuarios; no toca las tablas de la encuesta (RF-11). */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `users` (" +
                        "`usuario` TEXT NOT NULL, " +
                        "`hash` TEXT NOT NULL, " +
                        "`sal` TEXT NOT NULL, " +
                        "`iteraciones` INTEGER NOT NULL, " +
                        "`creadoEn` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`usuario`))"
                )
            }
        }

        /**
         * Añade las tablas PERSONAL/CARGO/PERFIL de la muestra (Cargar muestra).
         * Migración aditiva: no altera las tablas existentes ni sus datos.
         * Los tipos TEXT/INTEGER coinciden con los que Room genera para las entidades.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `PERFIL` (" +
                        "`CODPERFIL` TEXT NOT NULL, " +
                        "`NOMPERFIL` TEXT NOT NULL, " +
                        "PRIMARY KEY(`CODPERFIL`))"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_PERFIL_NOMPERFIL` " +
                        "ON `PERFIL` (`NOMPERFIL`)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `CARGO` (" +
                        "`CARGO` TEXT NOT NULL, " +
                        "`NOMCARGO` TEXT, " +
                        "`ABRCARGO` TEXT, " +
                        "PRIMARY KEY(`CARGO`))"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `PERSONAL` (" +
                        "`DNI` TEXT NOT NULL, " +
                        "`PERFIL` TEXT NOT NULL, " +
                        "`NOMBRE` TEXT NOT NULL, " +
                        "`APEPATERNO` TEXT NOT NULL, " +
                        "`APEMATERNO` TEXT NOT NULL, " +
                        "`ODEI` TEXT NOT NULL, " +
                        "`DPTO` TEXT, " +
                        "`PROV` TEXT, " +
                        "`DIST` TEXT, " +
                        "`CLAVE` TEXT, " +
                        "`USUARIO` TEXT, " +
                        "`ACTIVO` INTEGER NOT NULL, " +
                        "`FECHAALTA` TEXT, " +
                        "`FECHABAJA` TEXT, " +
                        "`MOVIL` INTEGER, " +
                        "`reentrevista` INTEGER, " +
                        "`PBASICO` INTEGER, " +
                        "`PFINAL` INTEGER, " +
                        "`CARGO` TEXT NOT NULL, " +
                        "`FECHANAC` TEXT, " +
                        "`TELEFONO` TEXT, " +
                        "`ESTADOCIVIL` TEXT, " +
                        "`SEXO` TEXT, " +
                        "PRIMARY KEY(`DNI`), " +
                        "FOREIGN KEY(`PERFIL`) REFERENCES `PERFIL`(`CODPERFIL`) " +
                        "ON UPDATE NO ACTION ON DELETE NO ACTION, " +
                        "FOREIGN KEY(`CARGO`) REFERENCES `CARGO`(`CARGO`) " +
                        "ON UPDATE NO ACTION ON DELETE NO ACTION)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_PERSONAL_PERFIL` " +
                        "ON `PERSONAL` (`PERFIL`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_PERSONAL_CARGO` " +
                        "ON `PERSONAL` (`CARGO`)"
                )
            }
        }

        /**
         * Elimina la tabla de prueba `users` (RF-04, CA-11): la autenticación real
         * pasa a `PERSONAL`. No toca las tablas de la encuesta ni de la muestra.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `users`")
            }
        }

        /**
         * Carga muestras de viviendas (RF-20..RF-26): crea las tablas maestras y de
         * viviendas más `CARGA_MUESTRA`, y siembra las maestras desde los SQL de
         * `docs/entities`. Migración aditiva: no altera las tablas existentes.
         */
        fun migracion4a5(seeder: MaestrasSeeder): Migration = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(CREATE_DPTO)
                db.execSQL(CREATE_PROV)
                db.execSQL(CREATE_DISTRITO)
                db.execSQL(CREATE_ODEI)
                db.execSQL(CREATE_PAIS)
                db.execSQL(CREATE_PERIODO)
                db.execSQL(CREATE_CAMPO_CONGLOMERADO)
                db.execSQL(CREATE_CAMPO_VIVIENDA_MARCO)
                db.execSQL(CREATE_CAMPO_VIVIENDA_REEMPLAZO)
                db.execSQL(CREATE_PERSONA_PANEL)
                db.execSQL(CREATE_CAP100PANEL)
                db.execSQL(CREATE_CARGA_MUESTRA)
                CREAR_INDICES.forEach { db.execSQL(it) }
                seeder.sembrar(db)
            }
        }

        /**
         * Modo Online (RF-30): añade la tabla maestra `MES` y la siembra desde
         * `docs/entities/MES.sql`. Migración aditiva: no altera tablas existentes.
         */
        fun migracion5a6(seeder: MaestrasSeeder): Migration = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(CREATE_MES)
                seeder.sembrarMes(db)
            }
        }

        private const val CREATE_MES =
            "CREATE TABLE IF NOT EXISTS `MES` (`CODIGO` TEXT NOT NULL, `NOMBRE` TEXT, " +
                "PRIMARY KEY(`CODIGO`))"

        private const val CREATE_DPTO =
            "CREATE TABLE IF NOT EXISTS `DPTO` (`CCDD` TEXT NOT NULL, `NOMBREDD` TEXT NOT NULL, " +
                "PRIMARY KEY(`CCDD`))"

        private const val CREATE_PROV =
            "CREATE TABLE IF NOT EXISTS `PROV` (`CCDD` TEXT NOT NULL, `CCPP` TEXT NOT NULL, " +
                "`NOMBREPV` TEXT NOT NULL, PRIMARY KEY(`CCDD`, `CCPP`), " +
                "FOREIGN KEY(`CCDD`) REFERENCES `DPTO`(`CCDD`) ON UPDATE NO ACTION ON DELETE NO ACTION)"

        private const val CREATE_DISTRITO =
            "CREATE TABLE IF NOT EXISTS `DISTRITO` (`CCDD` TEXT NOT NULL, `CCPP` TEXT NOT NULL, " +
                "`CCDI` TEXT NOT NULL, `NOMBRE_DIST` TEXT, `NOMBRE_CA` TEXT, `CODODEI` TEXT, `ETAPA` TEXT, " +
                "PRIMARY KEY(`CCDD`, `CCPP`, `CCDI`), " +
                "FOREIGN KEY(`CCDD`, `CCPP`) REFERENCES `PROV`(`CCDD`, `CCPP`) " +
                "ON UPDATE NO ACTION ON DELETE NO ACTION)"

        private const val CREATE_ODEI =
            "CREATE TABLE IF NOT EXISTS `ODEI` (`ID` INTEGER NOT NULL, `CODODEI` TEXT NOT NULL, " +
                "`NOMODEI` TEXT NOT NULL, `CCDD` TEXT, `USUCRE` TEXT, `FECCRE` TEXT, `USUREG` TEXT, " +
                "`FECREG` TEXT, `FECENV` TEXT, PRIMARY KEY(`ID`), " +
                "FOREIGN KEY(`CCDD`) REFERENCES `DPTO`(`CCDD`) ON UPDATE NO ACTION ON DELETE NO ACTION)"

        private const val CREATE_PAIS =
            "CREATE TABLE IF NOT EXISTS `PAIS` (`CODPAIS` TEXT NOT NULL, `NOMPAIS` TEXT, " +
                "`CODCONTINENTE` TEXT, `USUCRE` TEXT, `FECCRE` TEXT, `USUREG` TEXT, `FECREG` TEXT, " +
                "`FECENV` TEXT, PRIMARY KEY(`CODPAIS`))"

        private const val CREATE_PERIODO =
            "CREATE TABLE IF NOT EXISTS `PERIODO` (`CODPER` TEXT NOT NULL, `NOMPER` TEXT, " +
                "PRIMARY KEY(`CODPER`))"

        private const val CREATE_CAMPO_CONGLOMERADO =
            "CREATE TABLE IF NOT EXISTS `CAMPO_CONGLOMERADO` (" +
                "`ID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `CONGLOME` TEXT NOT NULL, " +
                "`ANHO` TEXT, `MES` TEXT, `PERIODO` TEXT, `ESTRATO` TEXT, `DPTO` TEXT, `PROV` TEXT, " +
                "`DIST` TEXT, `ZONA` TEXT, `DOMINIO` TEXT, `TIPOSELECCION` TEXT, `ODEI` TEXT, " +
                "`AERINI` TEXT, `AERFIN` TEXT, `MUESTRA` TEXT, `SEDE` TEXT, `PRUEBA` INTEGER, " +
                "`COVID19` INTEGER, `USUARIODIG` TEXT, `FECHADIG` TEXT, `USUARIOMOD` TEXT, " +
                "`FECHAMOD` TEXT, `USUCRE` TEXT, `FECCRE` TEXT, `USUREG` TEXT, `FECREG` TEXT, " +
                "`FECENV` TEXT, " +
                "FOREIGN KEY(`ODEI`) REFERENCES `ODEI`(`CODODEI`) ON UPDATE NO ACTION ON DELETE NO ACTION, " +
                "FOREIGN KEY(`PERIODO`) REFERENCES `PERIODO`(`CODPER`) ON UPDATE NO ACTION ON DELETE NO ACTION)"

        private const val CREATE_CAMPO_VIVIENDA_MARCO =
            "CREATE TABLE IF NOT EXISTS `CAMPO_VIVIENDA_MARCO` (" +
                "`ID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `CONGLOME` TEXT NOT NULL, " +
                "`VIVIENDA` TEXT NOT NULL, `MANZANA` TEXT, `NROVIVIENDA` TEXT, `TIPOVIVIENDA` TEXT, " +
                "`CATVIA` TEXT, `NOMBREVIA` TEXT, `PUERTA` TEXT, `INTERIOR` TEXT, `PISO` TEXT, " +
                "`MZ` TEXT, `LOTE` TEXT, `KM` TEXT, `NROBLOCK` TEXT, `CODCCPP` TEXT, `NOMCCPP` TEXT, " +
                "`LATORIGEN_GPS` TEXT, `LONORIGEN_GPS` TEXT, `USUARIODIG` TEXT, `FECHADIG` TEXT, " +
                "`USUARIOMOD` TEXT, `FECHAMOD` TEXT, `activo` INTEGER, `USUCRE` TEXT, `FECCRE` TEXT, " +
                "`USUREG` TEXT, `FECREG` TEXT, `FECENV` TEXT, " +
                "FOREIGN KEY(`CONGLOME`) REFERENCES `CAMPO_CONGLOMERADO`(`CONGLOME`) " +
                "ON UPDATE NO ACTION ON DELETE NO ACTION)"

        private const val CREATE_CAMPO_VIVIENDA_REEMPLAZO =
            "CREATE TABLE IF NOT EXISTS `CAMPO_VIVIENDA_REEMPLAZO` (" +
                "`CONGLOME` TEXT NOT NULL, `VIVIENDA` TEXT NOT NULL, `MZN` TEXT, `TIPVIA` TEXT, " +
                "`NOMVIA` TEXT, `PTANRO` TEXT, `NROBLOCK` TEXT, `NROPISO` TEXT, `INTNRO` TEXT, " +
                "`INTALF` TEXT, `NROMZA` TEXT, `NROLOTE` TEXT, `NROKM` TEXT, `OCUPADA` TEXT, " +
                "`ORDEN` TEXT, `ORDENVIV` TEXT, `SELECCION` TEXT, `CONTIGUASEL` TEXT, `REEMPLAZO` TEXT, " +
                "`CONTIGUAREE` TEXT, `SELECCIONANT` TEXT, `ANHOANT` TEXT, `MESANT` TEXT, `DPTO` TEXT, " +
                "`ODEI` TEXT, `USOLOCAL` TEXT, `JEFEHOGAR` TEXT, `REEMPLAZOCAMPO` TEXT, `GRUPO` TEXT, " +
                "`NOMCCPP` TEXT, `AERINI` TEXT, `AERFIN` TEXT, `CODCCPP` TEXT, `LATITUD` TEXT, " +
                "`LONGITUD` TEXT, `USUCRE` TEXT, `FECCRE` TEXT, `USUREG` TEXT, `FECREG` TEXT, `FECENV` TEXT, " +
                "PRIMARY KEY(`CONGLOME`, `VIVIENDA`), " +
                "FOREIGN KEY(`CONGLOME`) REFERENCES `CAMPO_CONGLOMERADO`(`CONGLOME`) " +
                "ON UPDATE NO ACTION ON DELETE NO ACTION)"

        private const val CREATE_PERSONA_PANEL =
            "CREATE TABLE IF NOT EXISTS `PERSONA_PANEL` (" +
                "`CONGLOME` TEXT NOT NULL, `VIVIENDA` TEXT NOT NULL, `HOGAR` TEXT, " +
                "`CODPANEL` TEXT NOT NULL, `P201` TEXT, `P202A` TEXT, `P202B` TEXT, `P202C` TEXT, " +
                "`P203` TEXT, `P207` TEXT, `P208A` TEXT, `P301A` TEXT, `P301B` TEXT, `P401` TEXT, " +
                "`P558C` TEXT, `P558C_ESPECIFIQUE` TEXT, `USUCRE` TEXT, `FECCRE` TEXT, `USUREG` TEXT, " +
                "`FECREG` TEXT, `FECENV` TEXT, `P204` TEXT, `P401H1` TEXT, `P401H2` TEXT, `P401H3` TEXT, " +
                "`P401H4` TEXT, `P401H5` TEXT, `P401H6` TEXT, `P558A1` TEXT, `P558A2` TEXT, `P558A3` TEXT, " +
                "`P558A4` TEXT, `P558A5` TEXT, `P558A6` TEXT, `P401H11` TEXT, `P401H12` TEXT, " +
                "`P401H13` TEXT, `P401H14` TEXT, `P401H15` TEXT, `P401H16` TEXT, `P401H17` TEXT, " +
                "`P558C1` TEXT, `P558C1_ESPECIFIQUE` TEXT, " +
                "PRIMARY KEY(`CONGLOME`, `VIVIENDA`, `CODPANEL`), " +
                "FOREIGN KEY(`CONGLOME`, `VIVIENDA`) REFERENCES `CAMPO_VIVIENDA_MARCO`(`CONGLOME`, `VIVIENDA`) " +
                "ON UPDATE NO ACTION ON DELETE NO ACTION)"

        private const val CREATE_CAP100PANEL =
            "CREATE TABLE IF NOT EXISTS `CAP100PANEL` (" +
                "`ANHO` TEXT NOT NULL, `MES` TEXT NOT NULL, `CONGLOME` TEXT NOT NULL, " +
                "`VIVIENDA` TEXT NOT NULL, `HOGAR` TEXT NOT NULL, `UBIGEO` TEXT, `DOMINIO` TEXT, " +
                "`ESTRATO` TEXT, `P101` TEXT, `P102A` TEXT, `P103` TEXT, `P103A` TEXT, `P104` TEXT, " +
                "`P104A` TEXT, `P105A` TEXT, `P110` TEXT, `P110A` TEXT, `P111` TEXT, `P112_1` TEXT, " +
                "`P112_2` TEXT, `P112_3` TEXT, `P112_4` TEXT, `P112_5` TEXT, `P112_6` TEXT, `P112_7` TEXT, " +
                "`P112a` TEXT, `USUCRE` TEXT, `FECCRE` TEXT, `USUREG` TEXT, `FECREG` TEXT, `FECENV` TEXT, " +
                "`P106A` TEXT, `P113A` TEXT, `ALERT` TEXT, " +
                "PRIMARY KEY(`ANHO`, `MES`, `CONGLOME`, `VIVIENDA`, `HOGAR`))"

        private const val CREATE_CARGA_MUESTRA =
            "CREATE TABLE IF NOT EXISTS `CARGA_MUESTRA` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`tipo` TEXT NOT NULL, `anho` TEXT, `odei` TEXT, `mes` TEXT, `fechaHora` INTEGER NOT NULL)"

        /**
         * Índices que Room valida para las tablas nuevas (nombres y columnas
         * idénticos a los del esquema generado en `5.json`).
         */
        private val CREAR_INDICES = listOf(
            "CREATE INDEX IF NOT EXISTS `index_PROV_CCDD` ON `PROV` (`CCDD`)",
            "CREATE INDEX IF NOT EXISTS `index_DISTRITO_CCDD_CCPP` ON `DISTRITO` (`CCDD`, `CCPP`)",
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_ODEI_CODODEI` ON `ODEI` (`CODODEI`)",
            "CREATE INDEX IF NOT EXISTS `index_ODEI_CCDD` ON `ODEI` (`CCDD`)",
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_CAMPO_CONGLOMERADO_CONGLOME` " +
                "ON `CAMPO_CONGLOMERADO` (`CONGLOME`)",
            "CREATE INDEX IF NOT EXISTS `index_CAMPO_CONGLOMERADO_ODEI` " +
                "ON `CAMPO_CONGLOMERADO` (`ODEI`)",
            "CREATE INDEX IF NOT EXISTS `index_CAMPO_CONGLOMERADO_PERIODO` " +
                "ON `CAMPO_CONGLOMERADO` (`PERIODO`)",
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_CAMPO_VIVIENDA_MARCO_CONGLOME_VIVIENDA` " +
                "ON `CAMPO_VIVIENDA_MARCO` (`CONGLOME`, `VIVIENDA`)",
            "CREATE INDEX IF NOT EXISTS `index_CAMPO_VIVIENDA_MARCO_CONGLOME` " +
                "ON `CAMPO_VIVIENDA_MARCO` (`CONGLOME`)",
            "CREATE INDEX IF NOT EXISTS `index_CAMPO_VIVIENDA_REEMPLAZO_CONGLOME` " +
                "ON `CAMPO_VIVIENDA_REEMPLAZO` (`CONGLOME`)",
            "CREATE INDEX IF NOT EXISTS `index_PERSONA_PANEL_CONGLOME_VIVIENDA` " +
                "ON `PERSONA_PANEL` (`CONGLOME`, `VIVIENDA`)",
            "CREATE INDEX IF NOT EXISTS `index_CARGA_MUESTRA_tipo_anho_odei_mes` " +
                "ON `CARGA_MUESTRA` (`tipo`, `anho`, `odei`, `mes`)"
        )
    }
}
