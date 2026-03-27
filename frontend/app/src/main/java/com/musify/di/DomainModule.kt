package com.musify.di

import com.musify.data.repository.AuthRepositoryImpl
import com.musify.data.repository.MusicRepositoryImpl
import com.musify.data.repository.PlaylistRepositoryImpl
import com.musify.data.repository.UserRepositoryImpl
import com.musify.domain.repository.AuthRepository
import com.musify.domain.repository.MusicRepository
import com.musify.domain.repository.PlaylistRepository
import com.musify.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Domain dependency injection module
 * Binds repository implementations to their domain interfaces
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {
    
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
    
    @Binds
    @Singleton
    abstract fun bindMusicRepository(
        musicRepositoryImpl: MusicRepositoryImpl
    ): MusicRepository
    
    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(
        playlistRepositoryImpl: PlaylistRepositoryImpl
    ): PlaylistRepository
    
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}