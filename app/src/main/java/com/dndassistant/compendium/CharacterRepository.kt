package com.dndassistant.compendium

class CharacterRepository(private val characterDao: CharacterDao) {

    suspend fun getCharacter(id: Int): com.dndassistant.compendium.CharacterTable?{
        return characterDao.getById(id)
    }

    suspend fun getCharacter(name: String): com.dndassistant.compendium.CharacterTable? {
        return characterDao.getByName(name)
    }
}
