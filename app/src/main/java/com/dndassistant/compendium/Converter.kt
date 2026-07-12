package com.dndassistant.compendium

import androidx.room.TypeConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class Converter {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    fun characterToJson(character: SerializableCharacter): String =
        json.encodeToString(character)

    @TypeConverter
    fun stringToCharacter(data: String): SerializableCharacter =
        json.decodeFromString(data)
}

@Serializable
data class SerializableCharacter(
    val id: String,
    val name: String,
    val description: String,
    val characterClass: String,
    val characterSubclass: String)