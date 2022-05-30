package com.example.criminallintent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.example.criminallintent.database.CrimeDao
import com.example.criminallintent.database.CrimeDataBase
import com.example.criminallintent.database.migration_1_2
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(context: Context){

    private val executor = Executors.newSingleThreadExecutor()

    private val database: CrimeDataBase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDataBase::class.java,
        DATABASE_NAME
        ).addMigrations(migration_1_2)
        .build()

    private val Dao: CrimeDao = database.getDao()

    fun getCrime(id: UUID): LiveData<Crime?> = Dao.getCrime(id)

    fun getCrimes(): LiveData<List<Crime>> = Dao.getCrimes()

    fun updateCrime(crime: Crime) {
        executor.execute {
            Dao.updateCrime(crime)
        }
    }

    fun addCrime(crime: Crime) {
        executor.execute {
            Dao.addCrime(crime)
        }
    }



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