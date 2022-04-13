package com.example.criminallintent

import androidx.lifecycle.ViewModel

class CrimeListViewModel: ViewModel() {

    private val crimeRepository = CrimeRepository.getRepository()
    val crimesListLiveData = crimeRepository.getCrimes()

}