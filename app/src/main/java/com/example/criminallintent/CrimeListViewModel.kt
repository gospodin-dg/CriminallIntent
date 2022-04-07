package com.example.criminallintent

import androidx.lifecycle.ViewModel

class CrimeListViewModel: ViewModel() {
    /*val crimesList = mutableListOf<Crime>()

    init {
        for(i in 0 until 100){
            val crime = Crime()
            crime.title = "Crime #$i"
            crime.isSolved = i % 2 == 0
            crimesList += crime
        }
    }*/

    private val crimeRepository = CrimeRepository.getRepository()
    val crimesListLiveData = crimeRepository.getCrimes()

}