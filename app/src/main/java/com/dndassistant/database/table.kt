package com.dndassistant.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_character")
data class CharacterTable(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val title: String,

    val character: SerializableCharacter
)
