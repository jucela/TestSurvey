package gob.inei.appprueba.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gob.inei.appprueba.data.repositories.SurveyRepositoryImpl
import gob.inei.appprueba.data.sources.RawCatalogDataSource
import gob.inei.appprueba.data.sources.ResRawCatalogDataSource
import gob.inei.appprueba.domain.repositories.SurveyRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSurveyRepository(impl: SurveyRepositoryImpl): SurveyRepository

    @Binds
    @Singleton
    abstract fun bindRawCatalogDataSource(impl: ResRawCatalogDataSource): RawCatalogDataSource
}
