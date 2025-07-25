package ro.aenigma.services

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ro.aenigma.util.ContextExtensions.splitFilesFirstFitDecreasing
import javax.inject.Inject

class UriBatcher @Inject constructor(
    @param:ApplicationContext private val applicationContext: Context
){
    suspend fun split(uriStrings: List<String>): List<List<String>> {
        return withContext(Dispatchers.IO) {
            applicationContext.splitFilesFirstFitDecreasing(uriStrings)
        }
    }
}
