package com.example.enigma.ui.fragments.contacts

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.enigma.R
import com.example.enigma.adapters.ContactsAdapter
import com.example.enigma.databinding.FragmentContactsBinding
import com.example.enigma.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactsFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private val chatsAdapter by lazy { ContactsAdapter(findNavController()) }
    private lateinit var fragmentChatsBinding: FragmentContactsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        fragmentChatsBinding = FragmentContactsBinding.inflate(inflater, container, false)

        return fragmentChatsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        readContactsFromDatabase()
        setupToolbar()
    }

    private fun setupRecyclerView() {
        fragmentChatsBinding.recyclerview.adapter = chatsAdapter
        fragmentChatsBinding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
        fragmentChatsBinding.recyclerview.itemAnimator = null
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

    private fun setupToolbar()
    {
        fragmentChatsBinding.toolbar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId)
            {
                R.id.addContacts ->  {
                    val direction = ContactsFragmentDirections.actionContactsFragmentToAddContactsFragment()
                    findNavController().navigate(direction)
                    true
                }else -> false
            }
        }
    }
}
