package com.dndassistant.compendium

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [CompendiumTable::class],
    version = 1
)
@TypeConverters(Converter::class)
abstract class CompendiumDatabase: RoomDatabase() {

    abstract fun compendiumDao(): CompendiumDao

    companion object{
        @Volatile
        private var INSTANCE: CompendiumDatabase? = null

        fun getDatabase(context: Context): CompendiumDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CompendiumDatabase::class.java,
                    "DnDAssistant_compendium"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
