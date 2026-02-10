package com.dndassistant.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_character WHERE id = :id")
    suspend fun getById(id: Int): CharacterTable?

    @Query("SELECT * FROM user_character WHERE title = :title")
    suspend fun getByTitle(title: String): CharacterTable?

    @Query("SELECT * FROM user_character ORDER BY id ASC")
    fun readAllCharacters(): LiveData<List<CharacterTable>>

    @Query("SELECT * FROM user_character ORDER BY id ASC")
    fun getAllCharacters(): Flow<List<CharacterTable>>

    @Upsert
    suspend fun upsert(entry: CharacterTable)
}