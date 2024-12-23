package ro.aenigma.data

import javax.inject.Inject

class Repository @Inject constructor(
    localDataSource: LocalDataSource,
    remoteDataSource: RemoteDataSource
){
    val local = localDataSource

    val remote = remoteDataSource
}
