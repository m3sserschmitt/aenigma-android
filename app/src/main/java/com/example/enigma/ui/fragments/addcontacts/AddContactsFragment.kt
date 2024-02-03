package com.example.enigma.ui.fragments.addcontacts

import android.Manifest.permission.CAMERA
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.enigma.databinding.FragmentAddContactsBinding
import com.example.enigma.viewmodels.MainViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.json.JSONException
import org.json.JSONObject

class AddContactsFragment : Fragment(), ZXingScannerView.ResultHandler {

    private lateinit var mainViewModel: MainViewModel

    private var _binding: FragmentAddContactsBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddContactsBinding.inflate(inflater, container, false)

        setupBinding()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setScannerProperties()
        setupSwitchButtons()
    }

    private fun setupSwitchButtons()
    {
        binding.qrCodeSwitchButton.setOnClickListener {
            if(binding.qrCodeSwitchButton.text == "My code")
            {
                switchToCode()
            } else {
                switchToCamera()
            }
        }
    }

    private fun setupBinding()
    {
        binding.viewModel = mainViewModel
        binding.lifecycleOwner = this
    }

    private fun switchToCode()
    {
        binding.qrCodeScanner.stopCamera()
        binding.qrCodeSwitchButton.text = "Scan"
        binding.qrCodeScanner.visibility = View.INVISIBLE
        binding.qrCodeImageView.visibility = View.VISIBLE
        binding.qrScannerTextView.text = "Share the code to connect"
    }

    private fun switchToCamera()
    {
        binding.qrCodeScanner.startCamera()
        binding.qrCodeSwitchButton.text = "My code"
        binding.qrCodeScanner.visibility = View.VISIBLE
        binding.qrCodeImageView.visibility = View.INVISIBLE
        binding.qrScannerTextView.text = "Scan the code to connect"
    }

    private fun resumeCamera()
    {
        binding.qrCodeScanner.resumeCameraPreview(this)
    }

    private fun setScannerProperties() {
        binding.qrCodeScanner.setFormats(listOf(BarcodeFormat.QR_CODE))
        binding.qrCodeScanner.setAutoFocus(true)
        binding.qrCodeScanner.setResultHandler(this)
    }

    override fun onPause() {
        super.onPause()
        binding.qrCodeScanner.stopCameraPreview()
        binding.qrCodeScanner.stopCamera()
    }

    override fun onStop() {
        super.onStop()
        binding.qrCodeScanner.stopCameraPreview()
        binding.qrCodeScanner.stopCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(requireActivity(), CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            switchToCamera()
        } else {
            requestPermissionLauncher.launch(CAMERA)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            binding.qrCodeScanner.startCamera()
        }
    }

    override fun handleResult(result: Result?) {
        resumeCamera()
        val scannedData: String? = result?.text
        if(scannedData != null)
        {
            val jsonObject = JSONObject(scannedData)

            try {
                val publicKey = jsonObject.getString("publicKey")
                val guardAddress = jsonObject.getString("guardAddress")

                val destination = AddContactsFragmentDirections
                    .actionAddContactsFragmentToSaveContactBottomSheet(publicKey, guardAddress, true)

                findNavController().navigate(destination)
            } catch (ex: JSONException)
            {
                Toast.makeText(requireActivity(), "The code is invalid", Toast.LENGTH_SHORT)
                    .show()
            }
            catch (_: Exception)
            {
            }
            finally {

            }
        }
    }
}
