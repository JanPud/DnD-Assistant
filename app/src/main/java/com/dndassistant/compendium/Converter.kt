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
    fun characterToJson(character: SerializableCompendiumUnit): String =
        json.encodeToString(character)

    @TypeConverter
    fun stringToCharacter(data: String): SerializableCompendiumUnit =
        json.decodeFromString(data)
}

@Serializable
data class SerializableCompendiumUnit(
    val id: String,
    val name: String,
    val description: String,
    val characterClass: String,
    val characterSubclass: String)