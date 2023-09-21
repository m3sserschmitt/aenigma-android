package com.example.enigma.ui.fragments.addcontacts

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.enigma.databinding.FragmentAddContactsBinding
import com.example.enigma.util.Constants.Companion.HEX_ADDRESS
import com.example.enigma.util.Constants.Companion.PUBLIC_KEY
import com.example.enigma.util.QrCodeGenerator
import org.json.JSONObject

class AddContactsFragment : Fragment() {

    private lateinit var binding: FragmentAddContactsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddContactsBinding.inflate(inflater, container, false)

        showCode()

        return binding.root
    }

    private fun showCode()
    {
        val exportedData = ExportedData(HEX_ADDRESS, PUBLIC_KEY)
        var size = 0
        size = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.qrCodeImageView.height
        } else {
            binding.qrCodeImageView.width
        }

        val bitmap = QrCodeGenerator(size, size).encodeAsBitmap(exportedData.toString())

        binding.qrCodeImageView.setImageBitmap(bitmap)
    }

    class ExportedData constructor(
        private val address: String,
        private val publicKey: String) {

        override fun toString(): String {
            val data = JSONObject()

            try {
                data.put("address", address)
                data.put("publicKey", publicKey)
            } catch (e: Exception) {
                e.printStackTrace()
                return ""
            }

            return data.toString()
        }
    }
}
