package com.example.enigma.ui.fragments.chats

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.enigma.adapters.ChatsAdapter
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.databinding.FragmentChatsBinding
import com.example.enigma.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatsFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private val chatsAdapter by lazy { ChatsAdapter(requireActivity()) }
    private lateinit var fragmentChatsBinding: FragmentChatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
//         seedDatabase()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        fragmentChatsBinding = FragmentChatsBinding.inflate(inflater, container, false)

        setupRecyclerView()
        readContactsFromDatabase()
        return fragmentChatsBinding.root
    }

    private fun setupRecyclerView() {
        fragmentChatsBinding.recyclerview.adapter = chatsAdapter
        fragmentChatsBinding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun readContactsFromDatabase()
    {
        lifecycleScope.launch {
            mainViewModel.readContacts.observe(viewLifecycleOwner)
            {
                    contacts -> chatsAdapter.setData(contacts)
            }
        }
    }

    private fun seedDatabase()
    {
        mainViewModel.insertContact(ContactEntity("1234", "John", true))
        mainViewModel.insertContact(ContactEntity("1236", "Tom", false))
    }
}
