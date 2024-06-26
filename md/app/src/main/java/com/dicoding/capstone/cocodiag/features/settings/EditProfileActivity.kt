package com.dicoding.capstone.cocodiag.features.settings

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.capstone.cocodiag.R
import com.dicoding.capstone.cocodiag.common.InputValidator
import com.dicoding.capstone.cocodiag.common.ResultState
import com.dicoding.capstone.cocodiag.common.getAuthenticatedGlideUrl
import com.dicoding.capstone.cocodiag.common.showToast
import com.dicoding.capstone.cocodiag.common.uriToFile
import com.dicoding.capstone.cocodiag.data.local.model.UserModel
import com.dicoding.capstone.cocodiag.data.remote.payload.SignInParam
import com.dicoding.capstone.cocodiag.data.remote.payload.UpdateUserParam
import com.dicoding.capstone.cocodiag.databinding.ActivityEditProfileBinding
import com.dicoding.capstone.cocodiag.features.ViewModelFactory


class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    private val viewModel by viewModels<SettingsViewModel> {
        ViewModelFactory.getInstance(applicationContext)
    }

    private lateinit var userName: String
    private lateinit var userEmail: String
    private lateinit var userImageUrl: String
    private var userImage: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setData()

        binding.cvImageProfile.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_PICK /* or ACTION_GET_CONTENT */
            intent.type = "image/*"
            val chooser = Intent.createChooser(intent, "Choose a Picture")
            launcherIntentGallery.launch(chooser)
        }

        binding.btnCancel.setOnClickListener{
            startActivity(Intent(this@EditProfileActivity, SettingsActivity::class.java))
        }

        binding.btnSave.setOnClickListener {
            val name = binding.edName.text.toString()
            val email = binding.edEmail.text.toString()
            val password = viewModel.getUser().password

            if (validateInput(name, email)) {
                if (userImage != null) {
                    updateData(UpdateUserParam(name, email, password, uriToFile(userImage!!, this)))
                } else {
                    updateData(UpdateUserParam(name, email, password, null))
                }
            }
        }
    }


    private fun setData() {
        val currentUser = viewModel.getUser()
        userName = currentUser.name
        userEmail = currentUser.email
        userImageUrl = currentUser.imageProfile ?: ""

        binding.edName.setText(userName)
        binding.edEmail.setText(userEmail)

        if (userImageUrl != "") {
            val token = viewModel.getUser().token!!
            Glide.with(this)
                .load(getAuthenticatedGlideUrl(userImageUrl, token))
                .into(binding.cvImageProfile)
        }
    }

    private fun updateData(param: UpdateUserParam) {
        viewModel.updateUser(param).observe(this) { result ->
            when (result) {
                is ResultState.Loading -> {
                    setDisableBtnSave(true)
                }

                is ResultState.Error -> {
                    setDisableBtnSave(false)
                    showToast(this, result.error.message)
                }

                is ResultState.Success -> {
                    setDisableBtnSave(false)
                    userName = result.data.name
                    userEmail = result.data.email

                    binding.edName.setText(userName)
                    binding.edEmail.setText(userEmail)

                    reSignIn(SignInParam(userEmail, viewModel.getPasswordFromPreference()))

                    showToast(this, "User updated successfully")
                }
            }
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            userImage = result.data?.data
            userImage?.let { uri ->
                val imageBitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                } else {
                    val src = ImageDecoder.createSource(this.contentResolver, uri)
                    ImageDecoder.decodeBitmap(src).copy(Bitmap.Config.RGBA_F16, true)
                }
                binding.cvImageProfile.setImageBitmap(imageBitmap)
            }
        }
    }

    private fun reSignIn(param: SignInParam) {
        viewModel.reSignIn(param).observe(this) { result ->
            when (result) {
                is ResultState.Loading -> {
                    setDisableBtnSave(true)
                }

                is ResultState.Error -> {
                    setDisableBtnSave(false)
                    showToast(this, result.error.message)
                }

                is ResultState.Success -> {
                    setDisableBtnSave(false)

                    val currentUser = UserModel(
                        result.data.id,
                        result.data.name,
                        result.data.email,
                        param.password,
                        result.data.imageProfile,
                        result.data.token,
                        true
                    )
                    viewModel.savedUser(currentUser)
                }
            }
        }
    }

    private fun validateInput(
        name: String,
        email: String,
    ): Boolean {
        var isValid = true

        if (!InputValidator.isValidName(name)) {
            binding.edName.error = "Name is required"
            isValid = false
        } else {
            binding.edName.error = null
        }

        if (!InputValidator.isValidEmail(email)) {
            binding.edEmail.error = "Invalid email address"
            isValid = false
        } else {
            binding.edEmail.error = null
        }

        return isValid
    }

    private fun setDisableBtnSave(isDisable: Boolean) {
        if (isDisable) {
            binding.btnSave.text = getString(R.string.save_loading)
            binding.btnSave.isEnabled = false
        } else {
            binding.btnSave.text = getString(R.string.save)
            binding.btnSave.isEnabled = true
        }
    }
}