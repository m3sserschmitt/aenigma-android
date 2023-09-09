package com.example.enigma.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.work.*
import com.example.enigma.R
import com.example.enigma.ui.fragments.addcontacts.AddContactsFragment
import com.example.enigma.ui.fragments.chats.ChatsFragment
import com.example.enigma.data.network.SignalRClientWorker
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object
    {
        init {
            System.loadLibrary("cryptography-wrapper")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadFragment(ChatsFragment())
        setupNavigation()
        startSignalRWorker()
    }

    private  fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.navHostFragment,fragment)
        transaction.commit()
    }

    private fun startSignalRWorker()
    {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val signalRClientRequest =
            PeriodicWorkRequestBuilder<SignalRClientWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(this)
            .enqueue(signalRClientRequest)
    }

    private fun setupNavigation()
    {
        val bottomNav : BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.chatsFragment -> {
                    loadFragment(ChatsFragment())
                    true
                }
                R.id.addContactsFragment -> {
                    loadFragment(AddContactsFragment())
                    true
                }
                else -> {
                    false
                }
            }
        }
    }
}
