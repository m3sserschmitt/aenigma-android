package com.example.enigma.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.enigma.R
import com.example.enigma.adapters.ChatAdapter
import com.example.enigma.util.Constants.Companion.SELECTED_CHAT_ID
import com.example.enigma.viewmodels.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    private lateinit var chatViewModel: ChatViewModel

    private val chatAdapter by lazy { ChatAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        savedInstanceState?.putString(SELECTED_CHAT_ID, intent.getStringExtra(SELECTED_CHAT_ID))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setTitle()
        setupRecyclerView()
        readConversationFromDatabase()
        //seedDatabase()
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun setupRecyclerView() {
        val chatRecycleView = findViewById<RecyclerView>(R.id.chatRecycleView)

        chatRecycleView.adapter = chatAdapter
        chatRecycleView.layoutManager = LinearLayoutManager(this)
    }

    private fun setTitle()
    {
        title = ""

        chatViewModel.getContact()?.observe(this) {
            title = it.name
        }
    }

    private fun readConversationFromDatabase()
    {
        chatViewModel.readConversation()?.observe(this) {
            chatAdapter.setData(it)
        }
    }

    private fun seedDatabase()
    {
//        chatViewModel.insertMessage(MessageEntity("1234", true, "Hello", Date()))
//        chatViewModel.insertMessage(MessageEntity("1234", true, "How are u?", Date()))
//        chatViewModel.insertMessage(MessageEntity("1234", false, "Hi, I'm fine", Date()))
    }
}
