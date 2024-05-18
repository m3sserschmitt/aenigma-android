package com.example.enigma.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.enigma.ui.navigation.Screens
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationTracker @Inject constructor() {

    private val _currentRoute: MutableLiveData<String> = MutableLiveData(Screens.NO_SCREEN)

    val currentRoute: LiveData<String> = _currentRoute

    fun postCurrentRoute(route: String)
    {
        _currentRoute.postValue(route)
    }
}
