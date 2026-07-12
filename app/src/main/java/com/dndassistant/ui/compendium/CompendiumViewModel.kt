package com.dndassistant.ui.compendium

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dndassistant.compendium.CompendiumDatabase
import com.dndassistant.compendium.CompendiumRepository
import com.dndassistant.compendium.CompendiumTable

// 1. Pobieranie danych z repozytorium
// 2. Pobieranie grafiki z pliku binarnego

class CompendiumViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val compendium: CompendiumRepository
    val compendiumData: LiveData<List<CompendiumTable>>
    private val activeEntryId = ""

    init {
        val compendiumDao = CompendiumDatabase.getDatabase(application).compendiumDao()
        compendium = CompendiumRepository(compendiumDao)
        compendiumData = compendium.allEntries
    }

    fun selectCharacterEntry(id: String) {
        return
    }

    private fun loadActiveEntry() {
        return
    }

//    fun getActiveEntry() : SerializableCharacter {
    fun getActiveEntry() {
        return
    }

//    private fun getAllNames() : List<String> {
    private fun getAllNames() {
        return
    }

}