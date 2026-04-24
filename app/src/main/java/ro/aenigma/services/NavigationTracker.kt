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
            return route.split("/").first() == Screens.CONTACTS_ROOT_PATH
        }

        @JvmStatic
        fun isChatScreenRoute(route: String): Boolean
        {
            return route.split("/").first() == Screens.CHAT_ROOT_PATH
        }

        @JvmStatic
        fun isAddContactsScreenRoute(route: String): Boolean
        {
            return route.split("/").first() == Screens.ADD_CONTACTS_ROOT_PATH
        }
    }

    private val _currentRoute: MutableLiveData<String> = MutableLiveData(Screens.NO_SCREEN)

    val currentRoute: LiveData<String> = _currentRoute

    fun postCurrentRoute(route: String)
    {
        _currentRoute.postValue(route)
    }
}
