package com.dndassistant.ui.characterCreation

import android.os.Bundle
import androidx.navigation.NavArgs

data class CharacterCreationArgs_dep (
    val chName: String,
    val chLevel: Int,
    val chClass: String,
    val chSubclass: String
) : NavArgs{
    companion object {
        private const val KEY_NAME = "chName"
        private const val KEY_LEVEL = "chLevel"
        private const val KEY_CLASS = "chClass"
        private const val KEY_SUBCLASS = "chSubclass"

        fun fromBundle(bundle: Bundle?): CharacterCreationArgs_dep {
            val chName = bundle?.getString(KEY_NAME) ?: ""
            val chLevel = bundle?.getInt(KEY_LEVEL) ?: 0
            val chClass = bundle?.getString(KEY_CLASS) ?: ""
            val chSubclass = bundle?.getString(KEY_SUBCLASS) ?: ""
            return CharacterCreationArgs_dep(chName, chLevel, chClass, chSubclass)
        }

        fun toBundle(args: CharacterCreationArgs): Bundle {
            return Bundle().apply {
                putInt(KEY_LEVEL, args.chLevel)
                putString(KEY_CLASS, args.chClass)
                putString(KEY_SUBCLASS, args.chSubclass)
            }
        }
    }
}