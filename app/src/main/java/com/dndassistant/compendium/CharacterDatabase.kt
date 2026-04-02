package com.dndassistant.compendium

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [CharacterTable::class],
    version = 1
)
@TypeConverters(Converter::class)
abstract class CharacterDatabase: RoomDatabase() {

    abstract fun characterDao(): CharacterDao

    companion object{
        @Volatile
        private var INSTANCE: CharacterDatabase? = null

        fun getDatabase(context: Context): CharacterDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CharacterDatabase::class.java,
                    "DnDAssistant_compendium"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
