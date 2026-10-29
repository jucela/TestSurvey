package gob.inei.appprueba.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import gob.inei.appprueba.data.databases.EncuestaDatabase
import gob.inei.appprueba.data.databases.dao.AlternativeDao
import gob.inei.appprueba.data.databases.dao.AnswerDao
import gob.inei.appprueba.data.databases.dao.Cap100PanelDao
import gob.inei.appprueba.data.databases.dao.CargaMuestraDao
import gob.inei.appprueba.data.databases.dao.CargoDao
import gob.inei.appprueba.data.databases.dao.ConglomeradoDao
import gob.inei.appprueba.data.databases.dao.DistritoDao
import gob.inei.appprueba.data.databases.dao.DptoDao
import gob.inei.appprueba.data.databases.dao.FlowRuleDao
import gob.inei.appprueba.data.databases.dao.HogarDao
import gob.inei.appprueba.data.databases.dao.MarcoDao
import gob.inei.appprueba.data.databases.dao.MesDao
import gob.inei.appprueba.data.databases.dao.OdeiDao
import gob.inei.appprueba.data.databases.dao.PaisDao
import gob.inei.appprueba.data.databases.dao.PerfilDao
import gob.inei.appprueba.data.databases.dao.PeriodoDao
import gob.inei.appprueba.data.databases.dao.PersonaPanelDao
import gob.inei.appprueba.data.databases.dao.PersonalDao
import gob.inei.appprueba.data.databases.dao.ProvDao
import gob.inei.appprueba.data.databases.dao.QuestionDao
import gob.inei.appprueba.data.databases.dao.ViviendaDao
import gob.inei.appprueba.data.databases.dao.ViviendaMarcoDao
import gob.inei.appprueba.data.databases.dao.ViviendaReemplazoDao
import java.io.File
import java.util.UUID
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Sustituye `DatabaseModule` en las pruebas Hilt por una base Room in-memory
 * sembrada con `MarcoTestData` y un DataStore temporal (MA-17). No usa la base
 * ni las preferencias de la app.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object MarcoTestModule {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EncuestaDatabase {
        val db = Room.inMemoryDatabaseBuilder(context, EncuestaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        MarcoTestData.sembrar(db)
        return db
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = scope) {
            File(context.cacheDir, "marco_test_sesion_${UUID.randomUUID()}.preferences_pb")
        }

    @Provides
    fun provideQuestionDao(database: EncuestaDatabase): QuestionDao = database.questionDao()

    @Provides
    fun provideAlternativeDao(database: EncuestaDatabase): AlternativeDao = database.alternativeDao()

    @Provides
    fun provideFlowRuleDao(database: EncuestaDatabase): FlowRuleDao = database.flowRuleDao()

    @Provides
    fun provideAnswerDao(database: EncuestaDatabase): AnswerDao = database.answerDao()

    @Provides
    fun providePersonalDao(database: EncuestaDatabase): PersonalDao = database.personalDao()

    @Provides
    fun provideCargoDao(database: EncuestaDatabase): CargoDao = database.cargoDao()

    @Provides
    fun providePerfilDao(database: EncuestaDatabase): PerfilDao = database.perfilDao()

    @Provides
    fun provideDptoDao(database: EncuestaDatabase): DptoDao = database.dptoDao()

    @Provides
    fun provideProvDao(database: EncuestaDatabase): ProvDao = database.provDao()

    @Provides
    fun provideDistritoDao(database: EncuestaDatabase): DistritoDao = database.distritoDao()

    @Provides
    fun provideOdeiDao(database: EncuestaDatabase): OdeiDao = database.odeiDao()

    @Provides
    fun provideMesDao(database: EncuestaDatabase): MesDao = database.mesDao()

    @Provides
    fun providePaisDao(database: EncuestaDatabase): PaisDao = database.paisDao()

    @Provides
    fun providePeriodoDao(database: EncuestaDatabase): PeriodoDao = database.periodoDao()

    @Provides
    fun provideConglomeradoDao(database: EncuestaDatabase): ConglomeradoDao =
        database.conglomeradoDao()

    @Provides
    fun provideViviendaMarcoDao(database: EncuestaDatabase): ViviendaMarcoDao =
        database.viviendaMarcoDao()

    @Provides
    fun provideViviendaReemplazoDao(database: EncuestaDatabase): ViviendaReemplazoDao =
        database.viviendaReemplazoDao()

    @Provides
    fun providePersonaPanelDao(database: EncuestaDatabase): PersonaPanelDao =
        database.personaPanelDao()

    @Provides
    fun provideCap100PanelDao(database: EncuestaDatabase): Cap100PanelDao =
        database.cap100PanelDao()

    @Provides
    fun provideCargaMuestraDao(database: EncuestaDatabase): CargaMuestraDao =
        database.cargaMuestraDao()

    @Provides
    fun provideViviendaDao(database: EncuestaDatabase): ViviendaDao = database.viviendaDao()

    @Provides
    fun provideHogarDao(database: EncuestaDatabase): HogarDao = database.hogarDao()

    @Provides
    fun provideMarcoDao(database: EncuestaDatabase): MarcoDao = database.marcoDao()
}
