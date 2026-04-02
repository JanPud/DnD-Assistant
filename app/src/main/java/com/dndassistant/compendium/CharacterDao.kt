package com.dndassistant.compendium

import androidx.room.Dao
import androidx.room.Query

@Dao
interface CharacterDao {
    @Query("SELECT * FROM compendium_character WHERE id = :id")
    suspend fun getById(id: Int): com.dndassistant.compendium.CharacterTable?

    @Query("SELECT * FROM compendium_character WHERE name = :name")
    suspend fun getByName(name: String): com.dndassistant.compendium.CharacterTable?
}