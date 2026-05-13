package com.dndassistant.utilities

import androidx.cardview.widget.CardView
import com.dndassistant.database.SerializableSkillListElement

class Data_classes {

}

data class Character_Basic(val Ch_class: String, val Ch_subclass: String, var experience: Int, var Ch_level: Int, var proficiency: Int)
data class Stats(val S: Int, val D: Int, val V: Int, val I: Int, val W: Int, val C: Int){
    operator fun iterator(): Iterator<Int>{
        return listOf(S, D, V, I, W, C).iterator()
    }
}
data class Survivability(val AR: Int, val Initiative: Int, val Dodge: Int,
                         val HP: Int, val Shield: Int, val Energy: Int,
                         val Cur_HP: Int, val Cur_Sh: Int, val Cur_En: Int)

data class BattleParticipant(val HP_pool: Int, val HP_curr: Int, val SH_pool: Int, val SH_curr: Int, val Initiative: Int, val AR: Int)
data class BattleCardData(val name: String, val initiative: Int, val card: CardView?)

data class SkillListElement(
    var modifier: Int,
    var profState: ProfState,
    val name: String,
    val proficiency: Int
) {
    fun toSerializable(): SerializableSkillListElement {
        return SerializableSkillListElement(
            modifier = this.modifier,
            profState = this.profState,
            name = this.name,
            proficiency = this.proficiency
        )
    }
}
enum class ProfState(val code: Int) {
    ZERO(0),
    ONE(1),
    TWO(2);

    fun next(): ProfState = when(this){
        ZERO -> ONE
        ONE -> TWO
        TWO -> ZERO
    }

    companion object{
        fun fromCode(code: Int): ProfState =
            entries.firstOrNull { it.code == code } ?: ZERO
    }
}

class ToastEvent<out T>(private val content: T) {
    private var handled = false
    fun get(): T? = if (handled) null else {
        handled = true
        content
    }
}

val allSkills = mutableListOf<Pair<String, String>>(
    Pair("Acrobatics", "S"),
    Pair("Alchemy", "W"),
    Pair("Athletics", "S"),
    Pair("Cautious", "D"),
    Pair("Encyclopedia", "I"),
    Pair("History", "I"),
    Pair("Hand-Eye Coord.", "D"),
    Pair("Sleight of Hand", "D"),
    Pair("Medicine", "I"),
    Pair("Nature", "W"),
    Pair("Science", "I"),
    Pair("Strong Head", "V"),
    Pair("Pain threshold", "V"),
    Pair("Perception", "W"),
    Pair("Performance", "C"),
    Pair("Persuasion", "C"),
    Pair("Underworld", "C"),
    Pair("Rhetoric", "C"),
    Pair("Willpower", "W"),
    Pair("Strategy", "W"),
    Pair("Technology", "I"),
    Pair("Stealth", "D"),
    Pair("Investigation", "I"),
    Pair("Insight", "W"),
    Pair("Intimidation", "C")
)

val expLvl = listOf<Pair<Int, Int>>(
    Pair(1, 300),
    Pair(2, 900),
    Pair(3, 2700),
    Pair(4, 6500),
    Pair(5, 14000),
    Pair(6, 23000),
    Pair(7, 34000),
    Pair(8, 48000),
    Pair(9, 64000),
    Pair(10, 85000),
    Pair(11, 100000),
    Pair(12, 120000),
    Pair(13, 140000),
    Pair(14, 165000),
    Pair(15, 195000),
    Pair(16, 225000),
    Pair(17, 265000),
    Pair(18, 305000),
    Pair(19, 355000),
    Pair(20, 355000)

)
