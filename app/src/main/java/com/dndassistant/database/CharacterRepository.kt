package com.dndassistant.database

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

class CharacterRepository(private val userDao: UserDao) {

    val allCharacters: LiveData<List<CharacterTable>> = userDao.readAllCharacters()

    fun readAllCharacters(): LiveData<List<CharacterTable>>{
        return userDao.readAllCharacters()
    }

    suspend fun getCharacter(id: Int): CharacterTable?{
        return userDao.getById(id)
    }

    suspend fun getCharacter(title: String): CharacterTable?{
        return userDao.getByTitle(title)
    }

    fun getAllCharacters(): Flow<List<CharacterTable>>{
        return  userDao.getAllCharacters()
    }

    suspend fun addCharacter(entry: CharacterTable){
        userDao.upsert(entry)
    }
}