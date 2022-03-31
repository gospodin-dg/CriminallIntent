package com.example.criminallintent

import android.content.Context
import androidx.room.Room
import com.example.criminallintent.database.CrimeDao
import com.example.criminallintent.database.CrimeDataBase
import java.util.*

private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(context: Context){

    private val database: CrimeDataBase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDataBase::class.java,
        DATABASE_NAME
    ).build()

    private val Dao: CrimeDao = database.getDao()

    fun getCrime(id: UUID): Crime? = Dao.getCrime(id)

    fun getCrimes(): List<Crime> = Dao.getCrimes()

    companion object{
        private var INSTANCE: CrimeRepository? = null

        fun createRepository(context: Context){
            if (INSTANCE == null) {
                INSTANCE = CrimeRepository(context)
            }
        }

        fun getRepository(): CrimeRepository {
            return INSTANCE ?: throw IllegalStateException("CrimeRepository must be initialized")
        }

    }
}