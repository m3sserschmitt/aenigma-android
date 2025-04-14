package ro.aenigma.services

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ro.aenigma.ui.navigation.Screens
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationTracker @Inject constructor() {

    companion object {
        @JvmStatic
        fun isContactsScreenRoute(route: String): Boolean
        {
            return route.split("/").first() == Screens.CONTACTS_SCREEN_BASE_ROUTE
        }

        @JvmStatic
        fun isChatScreenRoute(route: String): Boolean
        {
            return route.split("/").first() == Screens.CHAT_SCREEN_BASE_ROUTE
        }

        @JvmStatic
        fun isAddContactsScreenRoute(route: String): Boolean
        {
            return route.split("/").first() == Screens.ADD_CONTACTS_BASE_ROUTE
        }
    }

    private val _currentRoute: MutableLiveData<String> = MutableLiveData(Screens.NO_SCREEN)

    val currentRoute: LiveData<String> = _currentRoute

    fun postCurrentRoute(route: String)
    {
        _currentRoute.postValue(route)
    }
}
