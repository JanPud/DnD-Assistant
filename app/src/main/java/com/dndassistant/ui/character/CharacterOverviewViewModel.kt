package com.dndassistant.ui.character

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.dndassistant.database.CharacterRepository
import com.dndassistant.database.CharacterTable
import com.dndassistant.database.UserDatabase
import kotlinx.coroutines.launch

class CharacterOverviewViewModel(application: Application): AndroidViewModel(application) {

    private val repository: CharacterRepository
    val characterData: LiveData<List<CharacterTable>>


    init {
        val userDao = UserDatabase.getDatabase(application).userDao()
        repository = CharacterRepository(userDao)
        characterData = repository.allCharacters
    }

    fun deleteCharacter(character: CharacterTable){
        viewModelScope.launch {
            repository.deleteCharacter(character)
        }
    }
}