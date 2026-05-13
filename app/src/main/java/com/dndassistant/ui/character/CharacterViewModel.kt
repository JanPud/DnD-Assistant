package com.dndassistant.ui.character

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dndassistant.utilities.ProfState
import com.dndassistant.utilities.SkillListElement
import com.dndassistant.utilities.ToastEvent
import com.dndassistant.utilities.allSkills

class CharacterViewModel : ViewModel() {

    data class CharacterClass(val Ch_class: String, val Ch_subclass: String, val experience: Int, val Ch_level: Int, val proficiency: Int)
    data class Attributes(val S: Int, val D: Int, val V: Int, val I: Int, val W: Int, val C: Int){

        fun iterator(): List<Int>{
            return listOf(S, D, V, I, W, C)
        }
    }
    data class Survivability(val AR: Int, val Initiative: Int, val Dodge: Int,
                             val HP: Int, val Shield: Int, val Energy: Int,
                             val Cur_HP: Int, val Cur_Sh: Int, val Cur_En: Int)

    private var initialized = false

    private val _chName = MutableLiveData<String>()
    private val _character = MutableLiveData<CharacterClass>()
    private val _attributes = MutableLiveData(Attributes(10,11,12,13,14,15))
    private val _modifiers = MutableLiveData<Attributes>()
    private val _survivability = MutableLiveData<Survivability>()
    private val _skillProficiencies: MutableLiveData<MutableList<SkillListElement>> =
        MutableLiveData(mutableListOf())
    private val _skillCost = MutableLiveData<Attributes>()
    private val _highCostEvent = MutableLiveData<ToastEvent<String>>()

    val chName: LiveData<String> = _chName
    val character: LiveData<CharacterClass> = _character
    val attributes: LiveData<Attributes> = _attributes
    val modifiers: LiveData<Attributes> = _modifiers
    val survivability: LiveData<Survivability> = _survivability
    val skillProficiencies: LiveData<MutableList<SkillListElement>> = _skillProficiencies
    val skillCost: LiveData<Attributes> = _skillCost
    val highCostEvent: LiveData<ToastEvent<String>> = _highCostEvent

    fun receiveCharacterCreationArgs(args: CharacterFragmentArgs){
        if (initialized) return
        initialized = true
        val chBasic : CharacterClass = when(args.characterData.characterLevel) {
            1 -> CharacterClass(args.characterData.characterClass, args.characterData.characterSubclass,0,args.characterData.characterLevel,2)
            2 -> CharacterClass(args.characterData.characterClass, args.characterData.characterSubclass,300,args.characterData.characterLevel,2)
            3 -> CharacterClass(args.characterData.characterClass, args.characterData.characterSubclass,900,args.characterData.characterLevel,2)
            4 -> CharacterClass(args.characterData.characterClass, args.characterData.characterSubclass,2700,args.characterData.characterLevel,2)
            5 -> CharacterClass(args.characterData.characterClass, args.characterData.characterSubclass,6500,args.characterData.characterLevel,3)
            6 -> CharacterClass(args.characterData.characterClass, args.characterData.characterSubclass,14000,args.characterData.characterLevel,3)
            7 -> CharacterClass(args.characterData.characterClass, args.characterData.characterSubclass,23000,args.characterData.characterLevel,3)
            8 -> CharacterClass(args.characterData.characterClass, args.characterData.characterSubclass,34000,args.characterData.characterLevel,3)
            9 -> CharacterClass(args.characterData.characterClass, args.characterData.characterSubclass,48000,args.characterData.characterLevel,4)
            10 -> CharacterClass(args.characterData.characterClass, args.characterData.characterSubclass,64000,args.characterData.characterLevel,4)
            11 -> CharacterClass(args.characterData.characterClass, args.characterData.characterSubclass,85000,args.characterData.characterLevel,4)
            12 -> CharacterClass(args.characterData.characterClass, args.characterData.characterSubclass,10000,args.characterData.characterLevel,4)
            13 -> CharacterClass(args.characterData.characterClass, args.characterData.characterSubclass,120000,args.characterData.characterLevel,5)
            14 -> CharacterClass(args.characterData.characterClass, args.characterData.characterSubclass,140000,args.characterData.characterLevel,5)
            15 -> CharacterClass(args.characterData.characterClass, args.characterData.characterSubclass,165000,args.characterData.characterLevel,5)
            16 -> CharacterClass(args.characterData.characterClass, args.characterData.characterSubclass,195000,args.characterData.characterLevel,5)
            17 -> CharacterClass(args.characterData.characterClass, args.characterData.characterSubclass,225000,args.characterData.characterLevel,6)
            18 -> CharacterClass(args.characterData.characterClass, args.characterData.characterSubclass,265000,args.characterData.characterLevel,6)
            19 -> CharacterClass(args.characterData.characterClass, args.characterData.characterSubclass,305000,args.characterData.characterLevel,6)
            20 -> CharacterClass(args.characterData.characterClass, args.characterData.characterSubclass,355000,args.characterData.characterLevel,6)
            else -> CharacterClass(args.characterData.characterClass, args.characterData.characterSubclass,0,1,2)
        }
        _character.value = chBasic
        _chName.value = args.characterData.name

        _attributes.value = when(args.characterData.characterSubclass) {
            "Researcher" -> Attributes(8, 14, 10, 15, 14, 10)
            "Implementer" -> Attributes(12, 14, 10, 14, 14, 8)
            "Dax" -> Attributes(15, 14, 13, 8, 12, 10)
            "Guard" -> Attributes(14, 14, 14, 8, 10, 12)
            "Fundamentalist" -> Attributes(8, 10, 13, 12, 15, 14)
            "Alchemist" -> Attributes(8, 12, 13, 14, 15, 10)
            "Assassin" -> Attributes(13, 15, 12, 10, 13, 10)
            "Outlaw" -> Attributes(13, 15, 14, 10, 12, 8)
            "Admiral" -> Attributes(10, 14, 14, 10, 13, 12)
            "Hot-Shot" -> Attributes(12, 14, 14, 10, 10, 13)
            "Emissary" -> Attributes(8, 10, 12, 13, 14, 15)
            "Virtuoso" -> Attributes(10, 12,12,12, 12 , 15)
            else -> Attributes(10, 10, 10, 10, 10, 10)
        }
        designateAttrCost()

        updateModifiers()

        _survivability.value = Survivability(10, 0, 0, 11, 12, 5+5*args.characterData.characterLevel, 11, 12, 10)

        updateSkillProf()
    }

    fun designateAttrCost() : Boolean{
        if (_attributes.value != null) {
            val cost = mutableListOf<Int>()
            for (attribute in _attributes.value!!.iterator()) {
                when (attribute){
                    in 0 .. 8 -> cost.add(0)
                    9 -> cost.add(1)
                    10 -> cost.add(2)
                    11 -> cost.add(3)
                    12 -> cost.add(4)
                    13 -> cost.add(5)
                    14 -> cost.add(7)
                    15 -> cost.add(9)
                    else -> {
                        Log.d("ChCreation", "Incorrect attribute value")
                        return false
                    }
                }
            }
            if (cost.sum()>27){
//                Toast.makeText(, "A", Toast.LENGTH_SHORT)
                _highCostEvent.value = ToastEvent("Maximum attribute cost reached")
                return false
            }
            _skillCost.value = Attributes(cost[0], cost[1], cost[2], cost[3], cost[4], cost[5])
            return true
        }
        return false
    }

    fun updateModifiers(){
        if (_attributes.value != null) {
            _modifiers.value = Attributes(
                ((_attributes.value!!.S - (_attributes.value!!.S % 2)) - 10) / 2,
                ((_attributes.value!!.D - (_attributes.value!!.D % 2)) - 10) / 2,
                ((_attributes.value!!.V - (_attributes.value!!.V % 2)) - 10) / 2,
                ((_attributes.value!!.I - (_attributes.value!!.I % 2)) - 10) / 2,
                ((_attributes.value!!.W - (_attributes.value!!.W % 2)) - 10) / 2,
                ((_attributes.value!!.C - (_attributes.value!!.C % 2)) - 10) / 2
            )
        }
    }

    fun updateSkillProf(){
        _skillProficiencies.value = mutableListOf()
        if (_skillProficiencies.value != null && _modifiers.value != null && _character.value != null) {
            for (skill in allSkills) {
                val name = _skillProficiencies.value!!.find { it.name==skill.first }?.name
                if (name!=skill.first){
                    when (skill.second) {
                        "S" -> {_skillProficiencies.value!!.add(SkillListElement(_modifiers.value!!.S, ProfState.ZERO, skill.first, _character.value!!.proficiency))}
                        "D" -> {_skillProficiencies.value!!.add(SkillListElement(_modifiers.value!!.D, ProfState.ZERO, skill.first, _character.value!!.proficiency))}
                        "V" -> {_skillProficiencies.value!!.add(SkillListElement(_modifiers.value!!.V, ProfState.ZERO, skill.first, _character.value!!.proficiency))}
                        "I" -> {_skillProficiencies.value!!.add(SkillListElement(_modifiers.value!!.I, ProfState.ZERO, skill.first, _character.value!!.proficiency))}
                        "W" -> {_skillProficiencies.value!!.add(SkillListElement(_modifiers.value!!.W, ProfState.ZERO, skill.first, _character.value!!.proficiency))}
                        "C" -> {_skillProficiencies.value!!.add(SkillListElement(_modifiers.value!!.C, ProfState.ZERO, skill.first, _character.value!!.proficiency))}
                    }
                }
            }
            _skillProficiencies.value!!.sortBy { it.name }
        }
    }
}