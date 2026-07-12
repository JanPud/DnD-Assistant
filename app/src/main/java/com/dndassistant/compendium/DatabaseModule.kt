package com.dndassistant.compendium

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        CompendiumDatabase::class.java,
        "compendium_database"
    ).createFromAsset("databases/compendium_character.db").build()

    @Singleton
    @Provides
    fun provideDao(database: CompendiumDatabase) = database.compendiumDao()
}