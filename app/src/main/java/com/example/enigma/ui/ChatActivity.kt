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
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.network.MessageDispatcher
import com.example.enigma.data.network.SignalRClient
import com.example.enigma.util.Constants.Companion.FOREIGN_HEX_ADDRESS
import com.example.enigma.util.Constants.Companion.SELECTED_CHAT_ID
import com.example.enigma.viewmodels.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    private lateinit var chatViewModel: ChatViewModel

    private val chatAdapter by lazy { ChatAdapter() }

    private lateinit var contact: ContactEntity

    @Inject
    lateinit var messageDispatcher: MessageDispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        savedInstanceState?.putString(SELECTED_CHAT_ID, intent.getStringExtra(SELECTED_CHAT_ID))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getContact()
        setupRecyclerView()
        setupSendButton()
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

        button.setOnClickListener {

            val text = textInput.text.toString()

            if (::contact.isInitialized && text.isNotEmpty())
            {
                CoroutineScope(Dispatchers.IO).launch {
                    messageDispatcher.sendMessage(text, contact)
                    chatViewModel.insertOutgoingMessage(text)
                }
            }

            textInput.text.clear()
        }
    }

    private fun getContact()
    {
        title = ""

        chatViewModel.getContact()?.observe(this) {
            contact = it
            title = contact.name
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
