package com.dndassistant.ui.battle

import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dndassistant.R
import com.google.android.material.chip.Chip
import kotlinx.serialization.Serializable

class BattleViewModel : ViewModel() {

    //should only store data not whole views
    val listOfParticipant = mutableListOf<CardView>()
//    val cardList: MutableList<BattleCardData> = mutableListOf()
    @Serializable
    data class CardData(val cardTitle: String, var health: Int, val healthPool: Int, var shield: Int, val shieldPool: Int, var armor: Int, var initiative: Int,
                        val effects: MutableList<String>, val effectsDuration: MutableList<String>)
    val cardDataList: MutableList<CardData> = mutableListOf()

    private val _cardLiveDataList: MutableLiveData<MutableList<CardData>> = MutableLiveData(mutableListOf())
    val cardLiveDataList: LiveData<MutableList<CardData>> get() = _cardLiveDataList

    var round: Int = 0

    val roundList = listOf("1 round", "2 rounds", "3 rounds", "4 rounds", "5 rounds", "6 rounds", "7 rounds", "8 rounds", "9 rounds", "10 rounds", "Indefinitely")
    val effectList = listOf("Fire", "Cold", "Electric", "Toxin", "Explosion", "Corrosion", "Gas", "Magnetic", "Radiation", "Viral",
        "Morale Boost", "Rally Damage Boost", "Leader Aura",
        "Blind", "Knockdown", "Speed up", "Halt", "Stun", "Bleed", "Critical Strike", "Frozen",
        "Charge",
        "Buff", "Debuff")

    fun readCardData(card: CardView?): CardData?{
        if (card == null){
            return null
        } else {
            val cardTitle = card.findViewById<TextView>(R.id.cardTitle)?.text.toString()
            val currentHealth =
                card.findViewById<TextView>(R.id.currentHealth)?.text.toString().toInt()
            val healthPool = card.findViewById<TextView>(R.id.healthPool)?.text.toString().substringAfter("/").toInt()
            val currentShield =
                card.findViewById<TextView>(R.id.currentShield)?.text.toString().toInt()
            val shieldPool = card.findViewById<TextView>(R.id.shieldPool)?.text.toString().substringAfter("/").toInt()
            val currentArmor =
                card.findViewById<TextView>(R.id.currentArmor)?.text.toString().toInt()
            val currentInitiative =
                card.findViewById<TextView>(R.id.currentInitiative)?.text.toString().toInt()

            val effectList = mutableListOf<String>()
            val effectDuration = mutableListOf<String>()

            for (index in 0 until card.findViewById<LinearLayout>(R.id.participant_effect_layout)!!.childCount) {
                val effectChip = card.findViewById<LinearLayout>(R.id.participant_effect_layout)
                    ?.getChildAt(index) as Chip

                val descriptor = effectChip.text.toString()
                val label = descriptor.substringBefore(": ")
                val value = descriptor.substringAfter(": ")
                if (value == "Indefinitely") {
                    effectList.add(label)
                    effectDuration.add(value)
                } else {
                    effectList.add(label)
                    effectDuration.add(value.replace(" rounds", ""))
                }
            }
            val data = CardData(cardTitle,currentHealth,healthPool,currentShield,shieldPool,currentArmor,currentInitiative, effectList, effectDuration)
            return data
        }
    }

    private val _syncData = MutableLiveData<MutableList<CardData>>()
    val syncData: LiveData<MutableList<CardData>> = _syncData

    fun sendBattleStateDataToActivity(isDataReady: Boolean){
        if (!isDataReady) return
        if (cardDataList.isEmpty()) return

        val data = cardDataList
        _syncData.value = data
    }

    fun sendBattleStateDataToActivity(isDataReady: Boolean, endpoint: String){
        if (!isDataReady) return
        if (cardDataList.isEmpty()) return

        val data = cardDataList
        _syncData.value = data
    }

    fun receiveBattleStateData(receivedCardDataList: List<CardData>){
        cardDataList.clear()
        cardDataList.addAll(receivedCardDataList)
        _cardLiveDataList.value = receivedCardDataList.toMutableList()
    }

    fun clearCardData(){
        _cardLiveDataList.value = mutableListOf()
        cardDataList.clear()
    }
}