package com.example.enigma.ui

import android.os.Bundle
import androidx.activity.viewModels
import com.example.enigma.databinding.ActivityMainBinding
import com.example.enigma.viewmodels.BaseViewModel
import com.example.enigma.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    init {
        System.loadLibrary("cryptography-wrapper")
    }

    private lateinit var _binding: ActivityMainBinding

    private val binding get() = _binding

    private val mainViewModel: MainViewModel by viewModels()

    override val viewModel: BaseViewModel get() = mainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        supportActionBar?.hide()
        observeConnection()
    }
}
