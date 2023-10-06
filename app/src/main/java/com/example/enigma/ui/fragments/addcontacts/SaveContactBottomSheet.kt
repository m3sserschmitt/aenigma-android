package com.example.enigma.ui.fragments.addcontacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.databinding.SaveContactBottomSheetBinding
import com.example.enigma.util.AddressHelper
import com.example.enigma.viewmodels.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SaveContactBottomSheet : BottomSheetDialogFragment() {

    private var _binding: SaveContactBottomSheetBinding? = null

    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel

    private val args: SaveContactBottomSheetArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = SaveContactBottomSheetBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        setFormInfo()
        setupSaveButton()

        return binding.root
    }

    private fun setFormInfo()
    {
        binding.contactNameEditText.requestFocus()
        binding.guardAddressEditText.setText(args.guardAddress)
        binding.contactAddressEditText
            .setText(AddressHelper.getHexAddressFromPublicKey(args.publicKey))
    }

    private fun setupSaveButton()
    {
        binding.saveContactButton.setOnClickListener {
            val contactName = binding.contactNameEditText.text
            if(contactName.isNotEmpty())
            {
                val contact = ContactEntity(binding.guardAddressEditText.text.toString(),
                    contactName.toString(),
                    args.publicKey,
                    false)

                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.insertContact(contact)
                }

                val destination = SaveContactBottomSheetDirections
                    .actionSaveContactBottomSheetToChatsFragment()
                findNavController().navigate(destination)
            }
            else
            {
                Toast.makeText(requireActivity(), "Please provide a name.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}
