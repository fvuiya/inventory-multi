package com.bsoft.inventorymanager.activities

import android.Manifest
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import com.bsoft.inventorymanager.R
import com.bsoft.inventorymanager.model.Customer
import com.bsoft.inventorymanager.viewmodels.CustomerViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.IOException

@AndroidEntryPoint
class AddEditCustomerSheet : BottomSheetDialogFragment() {

    private lateinit var viewModel: CustomerViewModel
    private var currentCustomer: Customer? = null
    
    private lateinit var itemImageView: ImageView
    private var imageUri: Uri? = null
    private var imageBase64: String? = null

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    companion object {
        private const val ARG_CUSTOMER_JSON = "ARG_CUSTOMER_JSON"

        @JvmStatic
        fun newInstance(customer: Customer?): AddEditCustomerSheet {
            val fragment = AddEditCustomerSheet()
            val args = Bundle()
            if (customer != null) {
                args.putString(ARG_CUSTOMER_JSON, Json.encodeToString(customer))
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[CustomerViewModel::class.java]
        
        arguments?.getString(ARG_CUSTOMER_JSON)?.let { json ->
            try {
                currentCustomer = Json.decodeFromString<Customer>(json)
                imageBase64 = currentCustomer?.photo
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        initializeLaunchers()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_customer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        itemImageView = view.findViewById(R.id.iv_person_image)
        val cameraButton: Button = view.findViewById(R.id.btn_camera)
        val galleryButton: Button = view.findViewById(R.id.btn_gallery)
        val nameEditText: EditText = view.findViewById(R.id.editTextPersonName)
        val phoneEditText: EditText = view.findViewById(R.id.editTextPersonContact)
        val addressEditText: EditText = view.findViewById(R.id.editTextPersonAddress)
        val ageEditText: EditText = view.findViewById(R.id.editTextPersonAge)
        val saveButton: Button = view.findViewById(R.id.buttonSave)
        val cancelButton: Button = view.findViewById(R.id.buttonCancel)

        cameraButton.setOnClickListener { requestPermissionLauncher.launch(Manifest.permission.CAMERA) }
        galleryButton.setOnClickListener { pickImageLauncher.launch("image/*") }

        currentCustomer?.let { customer ->
            nameEditText.setText(customer.name)
            phoneEditText.setText(customer.contactNumber)
            addressEditText.setText(customer.address)
            ageEditText.setText(customer.age.toString())
            saveButton.text = "Save"

            if (!customer.photo.isNullOrEmpty()) {
                try {
                    val decodedString = Base64.decode(customer.photo, Base64.DEFAULT)
                    val decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    itemImageView.setImageBitmap(decodedByte)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } ?: run {
            saveButton.text = "Add"
        }

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val address = addressEditText.text.toString().trim()
            val ageStr = ageEditText.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || address.isEmpty() || ageStr.isEmpty()) {
                Toast.makeText(context, "Please fill all required fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val age = try {
                ageStr.toInt()
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "Please enter a valid age.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create new or update existing
            val customerToSave = currentCustomer?.copy(
                name = name,
                contactNumber = phone,
                address = address,
                age = age,
                photo = imageBase64,
                isActive = true
            ) ?: Customer(
                documentId = "", // Firestore will generate if empty, or repo handles it
                name = name,
                contactNumber = phone,
                address = address,
                age = age,
                photo = imageBase64,
                isActive = true
            )

            viewModel.saveCustomer(customerToSave)
            
            // We observe the result in the ViewModel
            viewModel.operationSuccess.observe(viewLifecycleOwner) { isSuccess ->
                if (isSuccess == true) {
                    Toast.makeText(
                        context,
                        if (currentCustomer == null) "Customer added!" else "Customer updated!",
                        Toast.LENGTH_SHORT
                    ).show()
                    handleSuccess(customerToSave)
                    viewModel.resetOperationStatus()
                }
            }
        }

        cancelButton.setOnClickListener { dismiss() }
    }

    private fun handleSuccess(customer: Customer) {
        val returnDirectly = arguments?.getBoolean("RETURN_DIRECTLY", false) == true
        val parentActivity = activity
        
        if (returnDirectly) {
            // Logic for returning directly needs CreateSaleActivity to be updated to support KMP Customer
            // For now, we dismiss, and assume the caller observes the ViewModel or Fragment Result
             setFragmentResult("customer_update", Bundle())
             dismiss()
        } else if (parentActivity != null && parentActivity.intent?.getBooleanExtra("RETURN_ON_ADD", false) == true) {
            val resultIntent = android.content.Intent()
            resultIntent.putExtra("NEW_CUSTOMER_ID", customer.documentId)
            parentActivity.setResult(android.app.Activity.RESULT_OK, resultIntent)
            parentActivity.finish()
        } else {
            setFragmentResult("customer_update", Bundle())
            dismiss()
        }
    }

    private fun initializeLaunchers() {
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                launchCamera()
            } else {
                Toast.makeText(context, "Camera permission is required.", Toast.LENGTH_SHORT).show()
            }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
            if (result && imageUri != null) {
                processImageUri(imageUri!!)
            }
        }

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                processImageUri(uri)
            }
        }
    }

    private fun launchCamera() {
        val values = android.content.ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Photo")
            put(MediaStore.Images.Media.DESCRIPTION, "From Inventory Manager App")
        }
        imageUri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        imageUri?.let { takePictureLauncher.launch(it) }
    }

    private fun processImageUri(uri: Uri) {
        this.imageUri = uri
        try {
            val bitmap = getBitmapFromUri(uri)
            itemImageView.setImageBitmap(bitmap)
            val resizedBitmap = resizeBitmap(bitmap, 100, 100)
            val baos = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val data = baos.toByteArray()
            this.imageBase64 = Base64.encodeToString(data, Base64.DEFAULT)
        } catch (e: IOException) {
            Toast.makeText(context, "Failed to process image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, uri))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        var width = bitmap.width
        var height = bitmap.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxWidth
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxHeight
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
}
