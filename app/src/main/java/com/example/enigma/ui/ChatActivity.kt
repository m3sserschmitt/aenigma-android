package com.example.enigma.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
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

        setupRecyclerView()
        setupSendButton()
        readConversationFromDatabase()
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

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        chatRecycleView.layoutManager = layoutManager
        chatRecycleView.adapter = chatAdapter
        chatRecycleView.itemAnimator = null
    }

    private fun setupSendButton()
    {
        val button = findViewById<Button>(R.id.buttonSend)
        val textInput = findViewById<EditText>(R.id.messageTextInput)

        chatViewModel.contact?.observe(this) {
            chatViewModel.guard.observe(this)
            {
                button.setOnClickListener {
                    val text = textInput.text.toString()

                    if (text.isNotEmpty()) {
                        // TODO: Start a worker to send message
                    }

                    textInput.text.clear()
                }
            }
        }
    }

    private fun readConversationFromDatabase()
    {
        chatViewModel.conversation?.observe(this) {
            chatAdapter.setData(it)
            chatViewModel.markConversationAsRead()
        }
    }
}
