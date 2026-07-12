package com.dndassistant.compendium

import androidx.lifecycle.LiveData

class CompendiumRepository(private val compendiumDao: CompendiumDao) {

    val allEntries: LiveData<List<CompendiumTable>> = compendiumDao.readAllEntries()

    suspend fun getCharacter(id: Int): com.dndassistant.compendium.CompendiumTable?{
        return compendiumDao.getById(id)
    }

    suspend fun getCharacter(name: String): com.dndassistant.compendium.CompendiumTable? {
        return compendiumDao.getByName(name)
    }
}
