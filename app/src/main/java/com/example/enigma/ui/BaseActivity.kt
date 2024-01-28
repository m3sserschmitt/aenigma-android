package com.example.enigma.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.enigma.data.network.SignalRStatus
import com.example.enigma.viewmodels.BaseViewModel
import com.example.enigma.workers.GraphReaderWorker
import com.example.enigma.workers.SignalRClientWorker

abstract class BaseActivity: AppCompatActivity() {

    protected abstract val viewModel: BaseViewModel

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        SignalRClientWorker.startPeriodicSync(this)
    }

    override fun onResume() {
        super.onResume()
        observeConnection()
    }

    private fun startConnection()
    {
        GraphReaderWorker.startOneTimeWorkRequest(this)
        SignalRClientWorker.startConnection(this)
    }

    private val signalRStatusObserver = Observer<SignalRStatus> { signalrClientStatus ->
        when(signalrClientStatus) {
            is SignalRStatus.Disconnected,
            is SignalRStatus.Error,
            is SignalRStatus.NotConnected -> startConnection()
        }
    }

    protected fun observeConnection()
    {
        viewModel.signalRClientStatus.observe(this, signalRStatusObserver)
    }
}
