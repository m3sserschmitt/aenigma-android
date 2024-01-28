package com.example.enigma.ui

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.example.enigma.R
import com.example.enigma.adapters.ChatAdapter
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.util.Constants.Companion.SELECTED_CHAT_ID
import com.example.enigma.viewmodels.BaseViewModel
import com.example.enigma.viewmodels.ChatViewModel
import com.example.enigma.workers.MessageSenderWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatActivity : BaseActivity() {
    private lateinit var chatViewModel: ChatViewModel

    private val chatAdapter by lazy { ChatAdapter() }

    override val viewModel: BaseViewModel
        get() = chatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        savedInstanceState?.putString(SELECTED_CHAT_ID, intent.getStringExtra(SELECTED_CHAT_ID))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        observeConnection()
        setupRecyclerView()
        setupSendButton()
        observeConversation()
        observePaths()
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

    private fun setupSendButton() {
        val button = findViewById<Button>(R.id.buttonSend)
        val textInput = findViewById<EditText>(R.id.messageTextInput)

        button.setOnClickListener {
            val text = textInput.text.toString()

            if (text.isNotEmpty()) {
                scheduleMessageSending(text)
            }

            textInput.text.clear()
        }
    }

    private val pathsAvailableObserver = Observer<Boolean> { pathExists ->
        if (!pathExists) chatViewModel.calculatePath()
    }

    private val conversationObserver = Observer<List<MessageEntity>> { conversation ->
        chatAdapter.setData(conversation)
        chatViewModel.markConversationAsRead()
    }

    private fun observePaths()
    {
        chatViewModel.pathsExists.observe(this, pathsAvailableObserver)
    }

    private fun observeConversation() {
        chatViewModel.conversation.observe(this, conversationObserver)
    }

    private fun scheduleMessageSending(message: String)
    {
        val inputData = Data.Builder()
            .putString(MessageSenderWorker.DATA_PARAM, message)
            .putString(MessageSenderWorker.DESTINATION_PARAM, chatViewModel.chatId)
            // TODO: refactor not tu send public key every time
            .putBoolean(MessageSenderWorker.INCLUDE_PUBLIC_KEY, true)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<MessageSenderWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }
}
