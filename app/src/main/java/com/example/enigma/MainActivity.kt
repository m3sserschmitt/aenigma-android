package com.example.enigma

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    companion object {
              init {
         System.loadLibrary("cryptography-wrapper")
      }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadFragment(ChatsFragment())
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

    private  fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.navHostFragment,fragment)
        transaction.commit()
    }
}
