package com.dndassistant.compendium

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
interface CompendiumDao {
    @Query("SELECT * FROM compendium_character WHERE id = :id")
    suspend fun getById(id: Int): com.dndassistant.compendium.CompendiumTable?

    @Query("SELECT * FROM compendium_character WHERE name = :name")
    suspend fun getByName(name: String): com.dndassistant.compendium.CompendiumTable?

    @Query("SELECT * FROM compendium_character ORDER BY id ASC")
    fun readAllEntries(): LiveData<List<CompendiumTable>>
}