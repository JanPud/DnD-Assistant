package com.dndassistant

import androidx.cardview.widget.CardView

class Data_classes {

}

data class Character_Basic(val Ch_class: String, val Ch_subclass: String, val experience: Int, val Ch_level: Int, val proficiency: Int)
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