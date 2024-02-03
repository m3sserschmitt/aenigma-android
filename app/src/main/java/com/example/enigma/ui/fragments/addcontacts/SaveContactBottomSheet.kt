package com.example.enigma.ui.fragments.addcontacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
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

    private lateinit var _binding: SaveContactBottomSheetBinding

    private val binding get() = _binding

    private val viewModel: MainViewModel by activityViewModels()

    private val args: SaveContactBottomSheetArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = SaveContactBottomSheetBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setFormInfo()
        setupSaveButton()
    }

    private fun setFormInfo() {
        binding.contactNameEditText.requestFocus()
    }

    private fun setupSaveButton()
    {
        binding.saveContactButton.setOnClickListener {

            val contactName = binding.contactNameEditText.text.toString()
            val contactAddress = AddressHelper.getHexAddressFromPublicKey(args.publicKey)

            if(contactName.isNotEmpty())
            {
                val contact = ContactEntity(
                    contactAddress,
                    contactName,
                    args.publicKey,
                    args.guardAddress,
                    false)

                CoroutineScope(Dispatchers.IO).launch {
                    if(args.createNew) {
                        viewModel.insertContact(contact)
                    }
                    else
                    {
                        viewModel.updateContact(contact)
                    }
                }

                dismissNow()
            }
            else
            {
                Toast.makeText(requireActivity(), "Please provide a name.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}
