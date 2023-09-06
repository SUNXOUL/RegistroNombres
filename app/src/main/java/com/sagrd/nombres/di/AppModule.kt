package com.sagrd.nombres.di

import android.content.Context
import androidx.room.Room
import com.sagrd.nombres.PersonaDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn ( SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providePersonaDatabase(@ApplicationContext appContext: Context): PersonaDB =
        Room.databaseBuilder(
            appContext,
            PersonaDB::class.java,
            "Persona.db")
            .fallbackToDestructiveMigration()
            .build()
}