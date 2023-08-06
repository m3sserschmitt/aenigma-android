package com.example.enigma

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.enigma.crypto.CryptoProvider
import com.example.enigma.crypto.DataProvider

class MainActivity : AppCompatActivity() {

    companion object {
              init {
         System.loadLibrary("cryptography-wrapper")
      }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}