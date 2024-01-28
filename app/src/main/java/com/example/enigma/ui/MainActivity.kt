package com.example.enigma.ui

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.enigma.R
import com.example.enigma.viewmodels.BaseViewModel
import com.example.enigma.viewmodels.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    init {
        System.loadLibrary("cryptography-wrapper")
    }

    private lateinit var mainViewModel: MainViewModel

    override val viewModel: BaseViewModel get() = mainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        observeConnection()
        setupNavigation()
    }

    private fun setupNavigation()
    {
        val navController = this.findNavController(R.id.navHostFragment)
        val navView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        navView.setupWithNavController(navController)
    }
}
