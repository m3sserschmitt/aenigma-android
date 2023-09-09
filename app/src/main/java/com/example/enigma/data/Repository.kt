package com.example.enigma.data

import javax.inject.Inject
import javax.inject.Singleton

//@ActivityRetainedScoped
@Singleton
class Repository @Inject constructor(
    localDataSource: LocalDataSource
){
    val local = localDataSource
}
