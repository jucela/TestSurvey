package gob.inei.appprueba.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gob.inei.appprueba.data.repositories.AuthRepositoryImpl
import gob.inei.appprueba.data.repositories.CatalogoMuestraRepositoryImpl
import gob.inei.appprueba.data.repositories.MarcoRepositoryImpl
import gob.inei.appprueba.data.repositories.MuestraOnlineRepositoryImpl
import gob.inei.appprueba.data.repositories.MuestraRepositoryImpl
import gob.inei.appprueba.data.repositories.SurveyRepositoryImpl
import gob.inei.appprueba.data.sources.RawCatalogDataSource
import gob.inei.appprueba.data.sources.ResRawCatalogDataSource
import gob.inei.appprueba.domain.repositories.AuthRepository
import gob.inei.appprueba.domain.repositories.CatalogoMuestraRepository
import gob.inei.appprueba.domain.repositories.MarcoRepository
import gob.inei.appprueba.domain.repositories.MuestraOnlineRepository
import gob.inei.appprueba.domain.repositories.MuestraRepository
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
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindMarcoRepository(impl: MarcoRepositoryImpl): MarcoRepository
    @Binds
    @Singleton
    abstract fun bindMuestraRepository(impl: MuestraRepositoryImpl): MuestraRepository

    @Binds
    @Singleton
    abstract fun bindMuestraOnlineRepository(
        impl: MuestraOnlineRepositoryImpl
    ): MuestraOnlineRepository

    @Binds
    @Singleton
    abstract fun bindCatalogoMuestraRepository(
        impl: CatalogoMuestraRepositoryImpl
    ): CatalogoMuestraRepository

    @Binds
    @Singleton
    abstract fun bindRawCatalogDataSource(impl: ResRawCatalogDataSource): RawCatalogDataSource
}
