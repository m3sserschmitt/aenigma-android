package com.example.enigma.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.enigma.data.network.SignalRClient
import com.example.enigma.data.network.SignalRStatus

open class BaseViewModel constructor(
    application: Application,
    signalRClient: SignalRClient
): AndroidViewModel(application) {

    val signalRClientStatus: LiveData<SignalRStatus> = signalRClient.status
}
