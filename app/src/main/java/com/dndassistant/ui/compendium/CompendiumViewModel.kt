package com.dndassistant.ui.compendium

import android.app.Application
import android.graphics.Bitmap
import androidx.collection.LruCache
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dndassistant.compendium.CompendiumDatabase
import com.dndassistant.compendium.CompendiumRepository
import com.dndassistant.compendium.CompendiumTable
import com.dndassistant.compendium.DatabaseModule
import com.dndassistant.utilities.BinaryReader

// 1. Pobieranie danych z repozytorium
// 2. Pobieranie grafiki z pliku binarnego

class CompendiumViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val compendium: CompendiumRepository
    val compendiumData: LiveData<List<CompendiumTable>>
    private val activeEntryId = ""
    private val binaryReader = BinaryReader.loadFromAssets(application, "binaries/Warframes.fook")
    private val cache = object : LruCache<String, Bitmap>(20) {
        override fun entryRemoved(
            evicted: Boolean,
            key: String,
            oldValue: Bitmap,
            newValue: Bitmap?
        ) {
            oldValue.recycle()
        }
    }

    init {
        val compendiumDao = DatabaseModule.provideDatabase(application).compendiumDao()
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

    fun getBitmap(name: String, width : Int=256, height: Int=256): Bitmap {
        cache[name]?.let { return it }

        return binaryReader.loadBitmap(name, width, height).also {
            cache.put(name, it)
        }
    }

}