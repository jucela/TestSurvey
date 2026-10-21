package gob.inei.appprueba.domain.repository

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TestMarcoModule {

    @Provides
    @Singleton
    fun provideMarcoRepository(): MarcoRepository = EmptyMarcoRepository
}