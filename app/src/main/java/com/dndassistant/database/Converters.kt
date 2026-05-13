package com.dndassistant.database

import androidx.room.TypeConverter
import com.dndassistant.utilities.ProfState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class Converters {
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
data class SerializableSkillListElement(
    var modifier: Int,
    var profState: ProfState,
    val name: String,
    val proficiency: Int
)

@Serializable
data class SerializableCharacter(
    val name: String,
    val characterClass: String,
    val characterSubclass: String,
    val experience: Int,
    val characterLevel: Int,
    val proficiency: Int,
    val S: Int,
    val D: Int,
    val V: Int,
    val I: Int,
    val W: Int,
    val C: Int,
    val AR: Int,
    val Initiative: Int,
    val Dodge: Int,
    val HP: Int,
    val Shield: Int,
    val Energy: Int,
    val Cur_HP: Int,
    val Cur_Sh: Int,
    val Cur_En: Int,
    val skillList: List<SerializableSkillListElement>,
    val image: String? = null) : java.io.Serializable

