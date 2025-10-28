package com.dndassistant.ui.character

import android.R
import android.view.animation.Transformation
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class CharacterViewModel : ViewModel() {

    data class Character_Class(val Ch_class: R.string, val Ch_subclass: R.string, val experience: Int, val Ch_level: Int, val proficiency: Int)
    data class Stats(val S: Int, val D: Int, val V: Int, val I: Int, val W: Int, val C: Int)
    data class Survivability(val AR: Int, val Initiative: Int, val Dodge: Int,
                             val HP: Int, val Shield: Int, val Energy: Int,
                             val Cur_HP: Int, val Cur_Sh: Int, val Cur_En: Int)


    private val _stats = MutableLiveData(Stats(10,11,12,13,14,15))
    private val _modifiers = MutableLiveData<Stats>()

    val stats: LiveData<Stats> = _stats

    val strength: LiveData<Int> = _stats.map { it.S }
    val dexterity: LiveData<Int> = _stats.map { it.D }
    val vitality: LiveData<Int> = _stats.map { it.V }
    val intelligence: LiveData<Int> = _stats.map { it.I }
    val wisdom: LiveData<Int> = _stats.map { it.W }
    val charisma: LiveData<Int> = _stats.map { it.C }


}