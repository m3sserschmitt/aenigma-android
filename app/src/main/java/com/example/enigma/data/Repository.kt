package com.example.enigma.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    localDataSource: LocalDataSource,
    remoteDataSource: RemoteDataSource
){
    val local = localDataSource

    val remote = remoteDataSource
}
