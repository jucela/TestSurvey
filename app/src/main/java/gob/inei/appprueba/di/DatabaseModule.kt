package gob.inei.appprueba.di

import android.content.Context
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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EncuestaDatabase =
        Room.databaseBuilder(context, EncuestaDatabase::class.java, "encuesta.db")
            .build()

    @Provides
    fun provideQuestionDao(database: EncuestaDatabase): QuestionDao = database.questionDao()

    @Provides
    fun provideAlternativeDao(database: EncuestaDatabase): AlternativeDao = database.alternativeDao()

    @Provides
    fun provideFlowRuleDao(database: EncuestaDatabase): FlowRuleDao = database.flowRuleDao()

    @Provides
    fun provideAnswerDao(database: EncuestaDatabase): AnswerDao = database.answerDao()
}
