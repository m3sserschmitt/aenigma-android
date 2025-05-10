package ro.aenigma.services

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import ro.aenigma.data.Repository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TorServiceController @Inject constructor(
    private val torServiceManager: TorServiceManager,
    private val repository: Repository
) {
    fun observeTorService(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            combine(
                repository.local.useTor,
                torServiceManager.torStatus
            ) { useTor, torStatus ->
                Pair(useTor, torStatus)
            }.distinctUntilChanged().collect { (useTor, torStatus) ->
                when {
                    useTor && (torStatus is TorStatus.Off || torStatus is TorStatus.Idle) -> {
                        torServiceManager.start(startForeground = true)
                    }

                    !useTor && torStatus is TorStatus.On -> {
                        torServiceManager.stop()
                    }
                }
            }
        }
    }
}
