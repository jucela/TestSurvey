package gob.inei.appprueba.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
import gob.inei.appprueba.data.sources.MaestrasSeeder
import gob.inei.appprueba.data.sources.sesionDataStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        seeder: MaestrasSeeder
    ): EncuestaDatabase =
        Room.databaseBuilder(context, EncuestaDatabase::class.java, "encuesta.db")
            .addMigrations(
                EncuestaDatabase.MIGRATION_1_2,
                EncuestaDatabase.MIGRATION_2_3,
                EncuestaDatabase.MIGRATION_3_4,
                EncuestaDatabase.migracion4a5(seeder),
                EncuestaDatabase.migracion5a6(seeder),
                EncuestaDatabase.MIGRATION_6_7,
                EncuestaDatabase.MIGRATION_7_8
            )
            .addCallback(
                object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        seeder.sembrar(db)
                        seeder.sembrarMes(db)
                    }
                }
            )
            .build()

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
    @Provides
    @Singleton
    fun provideSessionDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.sesionDataStore
}
