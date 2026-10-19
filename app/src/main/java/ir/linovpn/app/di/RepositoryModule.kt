package ir.linovpn.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.linovpn.app.data.repository.ProfileRepositoryImpl
import ir.linovpn.app.data.repository.ServerRepositoryImpl
import ir.linovpn.app.data.repository.SettingsRepositoryImpl
import ir.linovpn.app.domain.repository.ProfileRepository
import ir.linovpn.app.domain.repository.ServerRepository
import ir.linovpn.app.domain.repository.SettingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindServerRepository(impl: ServerRepositoryImpl): ServerRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
