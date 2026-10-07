package gob.inei.appprueba.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import gob.inei.appprueba.data.databases.EncuestaDatabase
import gob.inei.appprueba.data.databases.dao.AlternativeDao
import gob.inei.appprueba.data.databases.dao.AnswerDao
import gob.inei.appprueba.data.databases.dao.FlowRuleDao
import gob.inei.appprueba.data.databases.dao.QuestionDao
import gob.inei.appprueba.data.databases.dao.UserDao
import gob.inei.appprueba.data.sources.sesionDataStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EncuestaDatabase =
        Room.databaseBuilder(context, EncuestaDatabase::class.java, "encuesta.db")
            .addMigrations(EncuestaDatabase.MIGRATION_1_2)
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
    fun provideUserDao(database: EncuestaDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideSessionDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.sesionDataStore
}
