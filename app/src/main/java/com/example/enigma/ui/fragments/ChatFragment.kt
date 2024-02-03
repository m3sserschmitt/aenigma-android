package com.example.enigma.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.example.enigma.R
import com.example.enigma.adapters.ChatAdapter
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.databinding.FragmentChatBinding
import com.example.enigma.viewmodels.ChatViewModel
import com.example.enigma.workers.MessageSenderWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private val chatViewModel: ChatViewModel by activityViewModels()

    private val chatAdapter by lazy { ChatAdapter() }

    private lateinit var _binding: FragmentChatBinding

    private val args: ChatFragmentArgs by navArgs()

    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentChatBinding.inflate(layoutInflater, container, false)

        loadViewModel()
        setupBinding()

        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        setupSendButton()
        observeConversation()
        observePaths()
        observeContact()
        setupToolbar()
    }

    private fun loadViewModel()
    {
        chatViewModel.load(args.chatId)
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        binding.chatRecycleView.layoutManager = layoutManager
        binding.chatRecycleView.adapter = chatAdapter
        binding.chatRecycleView.itemAnimator = null
    }

    private fun setupSendButton() {

        binding.buttonSend.setOnClickListener {
            val text = binding.messageTextInput.text.toString()

            if (text.isNotEmpty()) {
                scheduleMessageSending(text)
            }

            binding.messageTextInput.text.clear()
        }
    }

    private fun setupToolbar()
    {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupBinding()
    {
        binding.viewModel = chatViewModel
        binding.lifecycleOwner = this
    }

    private val pathsAvailableObserver = Observer<Boolean> { pathExists ->
        if (!pathExists) chatViewModel.calculatePath()
    }

    private val conversationObserver = Observer<List<MessageEntity>> { conversation ->
        chatAdapter.setData(conversation)
        chatViewModel.markConversationAsRead()
    }

    private val contactObserver = Observer<ContactEntity?> { contact ->
        val navController = findNavController()
        if(navController.currentDestination?.id != R.id.saveContactBottomSheet
            && contact != null
            && contact.name.isEmpty())
        {
            val direction = ChatFragmentDirections.actionChatFragmentToSaveContactBottomSheet(
                contact.publicKey,
                contact.guardHostname,
                false)
            navController.navigate(direction)
        }
    }

    private fun observePaths()
    {
        lifecycleScope.launch {
            chatViewModel.pathsExists.observe(viewLifecycleOwner, pathsAvailableObserver)
        }
    }

    private fun observeConversation() {
        lifecycleScope.launch {
            chatViewModel.conversation.observe(viewLifecycleOwner, conversationObserver)
        }
    }

    private fun observeContact()
    {
        lifecycleScope.launch {
            chatViewModel.contact.observe(viewLifecycleOwner, contactObserver)
        }
    }

    private fun scheduleMessageSending(message: String)
    {
        val inputData = Data.Builder()
            .putString(MessageSenderWorker.DATA_PARAM, message)
            .putString(MessageSenderWorker.DESTINATION_PARAM, args.chatId)
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

        WorkManager.getInstance(requireContext()).enqueue(workRequest)
    }
}
