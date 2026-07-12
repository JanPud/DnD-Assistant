package com.dndassistant.compendium

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "compendium_character")
data class CharacterTable(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val character: SerializableCharacter
)
