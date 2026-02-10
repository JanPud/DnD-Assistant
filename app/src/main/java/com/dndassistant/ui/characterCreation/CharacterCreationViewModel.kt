package com.dndassistant.ui.characterCreation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dndassistant.ProfState
import com.dndassistant.database.SerializableCharacter
import com.dndassistant.database.SerializableSkillListElement
import com.dndassistant.SkillListElement
import com.dndassistant.database.CharacterRepository
import com.dndassistant.database.CharacterTable
import com.dndassistant.database.UserDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CharacterCreationViewModel(application: Application): AndroidViewModel(application) {
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

//    private val args by navArgs<CharacterCreationArgs>()
    private val _chName = MutableLiveData<String>()
    private val _character = MutableLiveData<CharacterClass>()
    private val _attributes = MutableLiveData(Attributes(10,11,12,13,14,15))
    private val _modifiers = MutableLiveData<Attributes>()
    private val _survivability = MutableLiveData<Survivability>()
    private val _skillProficiencies: MutableLiveData<MutableList<SkillListElement>> =
        MutableLiveData(mutableListOf())
    private val _skillCost = MutableLiveData<Attributes>()
    private val _highCostEvent = MutableLiveData<ToastEvent<String>>()
    private val repository: CharacterRepository
    private val allCharacters: LiveData<List<CharacterTable>>
    private val allCharactersFlow: StateFlow<List<CharacterTable>>
    private val allCharactersLocal: MutableList<CharacterTable> = mutableListOf()

    init {
        val userDao = UserDatabase.getDatabase(application).userDao()
        repository = CharacterRepository(userDao)
        allCharacters = repository.allCharacters
        allCharactersFlow =
            repository.getAllCharacters()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
        viewModelScope.launch {
            allCharactersFlow.collect { character ->
                allCharactersLocal.clear()
                for (entry in character){
                    allCharactersLocal.add(entry)
                }
            }
        }
    }

    val chName: LiveData<String> = _chName
    val character: LiveData<CharacterClass> = _character
    val attributes: LiveData<Attributes> = _attributes
    val modifiers: LiveData<Attributes> = _modifiers
    val survivability: LiveData<Survivability> = _survivability
    val skillProficiencies: LiveData<MutableList<SkillListElement>> = _skillProficiencies
    val skillCost: LiveData<Attributes> = _skillCost
    val highCostEvent: LiveData<ToastEvent<String>> = _highCostEvent

    fun receiveCharacterCreationArgs(name: String, chLevel: Int, chClass: String, chSubclass: String){
        if (initialized) return
        initialized = true
        val chBasic : CharacterClass = when(chLevel) {
            1 -> CharacterClass(chClass, chSubclass,0,chLevel,2)
            2 -> CharacterClass(chClass, chSubclass,300,chLevel,2)
            3 -> CharacterClass(chClass, chSubclass,900,chLevel,2)
            4 -> CharacterClass(chClass, chSubclass,2700,chLevel,2)
            5 -> CharacterClass(chClass, chSubclass,6500,chLevel,3)
            6 -> CharacterClass(chClass, chSubclass,14000,chLevel,3)
            7 -> CharacterClass(chClass, chSubclass,23000,chLevel,3)
            8 -> CharacterClass(chClass, chSubclass,34000,chLevel,3)
            9 -> CharacterClass(chClass, chSubclass,48000,chLevel,4)
            10 -> CharacterClass(chClass, chSubclass,64000,chLevel,4)
            11 -> CharacterClass(chClass, chSubclass,85000,chLevel,4)
            12 -> CharacterClass(chClass, chSubclass,10000,chLevel,4)
            13 -> CharacterClass(chClass, chSubclass,120000,chLevel,5)
            14 -> CharacterClass(chClass, chSubclass,140000,chLevel,5)
            15 -> CharacterClass(chClass, chSubclass,165000,chLevel,5)
            16 -> CharacterClass(chClass, chSubclass,195000,chLevel,5)
            17 -> CharacterClass(chClass, chSubclass,225000,chLevel,6)
            18 -> CharacterClass(chClass, chSubclass,265000,chLevel,6)
            19 -> CharacterClass(chClass, chSubclass,305000,chLevel,6)
            20 -> CharacterClass(chClass, chSubclass,355000,chLevel,6)
            else -> CharacterClass(chClass, chSubclass,0,1,2)
        }
        _character.value = chBasic
        _chName.value = name

        _attributes.value = when(chSubclass) {
            "Researcher" -> Attributes(8, 14, 10, 15, 14, 10)
            "Implementer" -> Attributes(11, 14, 10, 14, 14, 10)
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

        _survivability.value = Survivability(10, 0, 0, 11, 12, 5+5*chLevel, 11, 12, 10)

        updateSkillProf()
    }

    fun changeExp(value: Int){
        if (value < 0){
            Log.d("ChCreation", "Incorrect exp value provided")
        } else if (_character.value == null){
            Log.d("ChCreation", "No character data")
        } else {
            val chClass = _character.value!!.Ch_class
            val chSubclass = _character.value!!.Ch_subclass
            val chLevel = _character.value!!.Ch_level
            val prof = _character.value!!.proficiency
            _character.value = CharacterClass(chClass, chSubclass, value, chLevel, prof)
        }
    }

    fun changeAR(value: Int){
        if (value < 0){
            Log.d("ChCreation", "Incorrect AR value provided")
        } else if (_survivability.value == null){
            Log.d("ChCreation", "No survivability data")
        } else {
//            val ar = _survivability.value!!.AR
            val initiative = _survivability.value!!.Initiative
            val dodge = _survivability.value!!.Dodge
            val health = _survivability.value!!.HP
            val shield = _survivability.value!!.Shield
            val energy = _survivability.value!!.Energy
            val curHealth = _survivability.value!!.Cur_HP
            val curShield = _survivability.value!!.Cur_Sh
            val curEnergy = _survivability.value!!.Cur_En
            _survivability.value = Survivability(
                value,
                initiative,
                dodge,
                health,
                shield,
                energy,
                curHealth,
                curShield,
                curEnergy
            )
        }
    }

    fun changeHP(value: Int){
        if (value < 0){
            Log.d("ChCreation", "Incorrect HP value provided")
        } else if (_survivability.value == null){
            Log.d("ChCreation", "No survivability data")
        } else {
            val ar = _survivability.value!!.AR
            val initiative = _survivability.value!!.Initiative
            val dodge = _survivability.value!!.Dodge
//            val health = _survivability.value!!.HP
            val shield = _survivability.value!!.Shield
            val energy = _survivability.value!!.Energy
//            val curHealth = _survivability.value!!.Cur_HP
            val curShield = _survivability.value!!.Cur_Sh
            val curEnergy = _survivability.value!!.Cur_En
            _survivability.value = Survivability(
                ar,
                initiative,
                dodge,
                value,
                shield,
                energy,
                value,
                curShield,
                curEnergy
            )
        }
    }

    fun changeSH(value: Int){
        if (value < 0){
            Log.d("ChCreation", "Incorrect SH value provided")
        } else if (_survivability.value == null){
            Log.d("ChCreation", "No survivability data")
        } else {
            val ar = _survivability.value!!.AR
            val initiative = _survivability.value!!.Initiative
            val dodge = _survivability.value!!.Dodge
            val health = _survivability.value!!.HP
//            val shield = _survivability.value!!.Shield
            val energy = _survivability.value!!.Energy
            val curHealth = _survivability.value!!.Cur_HP
//            val curShield = _survivability.value!!.Cur_Sh
            val curEnergy = _survivability.value!!.Cur_En
            _survivability.value = Survivability(
                ar,
                initiative,
                dodge,
                health,
                value,
                energy,
                curHealth,
                value,
                curEnergy
            )
        }
    }

    fun changeEN(){
        if (_character.value == null || _survivability.value == null){
            Log.d("ChCreation", "No character data")
        } else {
            val ar = _survivability.value!!.AR
            val initiative = _survivability.value!!.Initiative
            val dodge = _survivability.value!!.Dodge
            val health = _survivability.value!!.HP
            val shield = _survivability.value!!.Shield
            val energy = 5+5*_character.value!!.Ch_level
            val curHealth = _survivability.value!!.Cur_HP
            val curShield = _survivability.value!!.Cur_Sh
            val curEnergy = 5+5*_character.value!!.Ch_level
            _survivability.value = Survivability(
                ar,
                initiative,
                dodge,
                health,
                shield,
                energy,
                curHealth,
                curShield,
                curEnergy
            )
        }
    }

    fun changeSkillProf(skillName: String){
        if (_skillProficiencies.value == null){
            Log.d("ChCreation", "No skill proficiency data found")
        } else {
            _skillProficiencies.value!!.find { it.name == skillName }?.profState?.next()
        }
    }

    fun resetAttributes(){
        if (_character.value != null) {
            _attributes.value = when (_character.value!!.Ch_subclass) {
                "Researcher" -> Attributes(8, 14, 10, 15, 14, 10)
                "Implementer" -> Attributes(11, 14, 10, 14, 14, 10)
                "Dax" -> Attributes(15, 14, 13, 8, 12, 10)
                "Guard" -> Attributes(14, 14, 14, 8, 10, 12)
                "Fundamentalist" -> Attributes(8, 10, 13, 12, 15, 14)
                "Alchemist" -> Attributes(8, 12, 13, 14, 15, 10)
                "Assassin" -> Attributes(13, 15, 12, 10, 13, 10)
                "Outlaw" -> Attributes(13, 15, 14, 10, 12, 8)
                "Admiral" -> Attributes(10, 14, 14, 10, 13, 12)
                "Hot-Shot" -> Attributes(12, 14, 14, 10, 10, 13)
                "Emissary" -> Attributes(8, 10, 12, 13, 14, 15)
                "Virtuoso" -> Attributes(10, 12, 12, 12, 12, 15)
                else -> Attributes(10, 10, 10, 10, 10, 10)
            }
        }
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

    fun changeAttribute(attribute: String, value: Int){
        if (_attributes.value != null) {
            when (attribute) {
                "S" -> {
                    val temp = Attributes(
                        value,
                        _attributes.value!!.D,
                        _attributes.value!!.V,
                        _attributes.value!!.I,
                        _attributes.value!!.W,
                        _attributes.value!!.C
                    )
                    _attributes.value = temp
                }
                "D" -> {
                    val temp = Attributes(
                        _attributes.value!!.S,
                        value,
                        _attributes.value!!.V,
                        _attributes.value!!.I,
                        _attributes.value!!.W,
                        _attributes.value!!.C
                    )
                    _attributes.value = temp
                }
                "V" -> {
                    val temp = Attributes(
                        _attributes.value!!.S,
                        _attributes.value!!.D,
                        value,
                        _attributes.value!!.I,
                        _attributes.value!!.W,
                        _attributes.value!!.C
                    )
                    _attributes.value = temp
                }
                "I" -> {
                    val temp = Attributes(
                        _attributes.value!!.S,
                        _attributes.value!!.D,
                        _attributes.value!!.V,
                        value,
                        _attributes.value!!.W,
                        _attributes.value!!.C
                    )
                    _attributes.value = temp
                }
                "W" -> {
                    val temp = Attributes(
                        _attributes.value!!.S,
                        _attributes.value!!.D,
                        _attributes.value!!.V,
                        _attributes.value!!.I,
                        value,
                        _attributes.value!!.C
                    )
                    _attributes.value = temp
                }
                "C" -> {
                    val temp = Attributes(
                        _attributes.value!!.S,
                        _attributes.value!!.D,
                        _attributes.value!!.V,
                        _attributes.value!!.I,
                        _attributes.value!!.W,
                        value
                    )
                    _attributes.value = temp
                }
                else -> Log.d("ChCreation", "Incorrect input provided")
            }
        }
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

    fun changeName(name: String){
        _chName.value = name
    }

    suspend fun collectCharacterFlow(): List<CharacterTable>{
        var a: List<CharacterTable> = listOf()
        repository.getAllCharacters().collect { character ->
            a = character
        }
        return a
    }

     fun saveCharacter(): Int {
        if (initialized) {
//            val characterList = collectCharacterFlow()
//            val checkName = allCharacters.value?.find { it.title == _chName.value }?.title
            val checkName = allCharactersLocal.find { it.title == _chName.value }?.title
            if (checkName == _chName.value) {
                return 2
            }

            val skillList = mutableListOf<SerializableSkillListElement>()
            for (skill in _skillProficiencies.value!!.toList()){
                skillList.add(skill.toSerializable())
            }
            val serializedCharacter = SerializableCharacter(
                _chName.value!!,
                _character.value!!.Ch_class,
                _character.value!!.Ch_subclass,
                _character.value!!.experience,
                _character.value!!.Ch_level,
                _character.value!!.proficiency,
                _attributes.value!!.S,
                _attributes.value!!.D,
                _attributes.value!!.V,
                _attributes.value!!.I,
                _attributes.value!!.W,
                _attributes.value!!.C,
                _survivability.value!!.AR,
                _survivability.value!!.Initiative,
                _survivability.value!!.Dodge,
                _survivability.value!!.HP,
                _survivability.value!!.Shield,
                _survivability.value!!.Energy,
                _survivability.value!!.Cur_HP,
                _survivability.value!!.Cur_Sh,
                _survivability.value!!.Cur_En,
                skillList
            )
            val characterEntry = CharacterTable(0, _chName.value!!, serializedCharacter)
            viewModelScope.launch {
                repository.addCharacter(characterEntry)
            }
            return 0
        }
        return 1
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

class ToastEvent<out T>(private val content: T) {
    private var handled = false
    fun get(): T? = if (handled) null else {
        handled = true
        content
    }
}

