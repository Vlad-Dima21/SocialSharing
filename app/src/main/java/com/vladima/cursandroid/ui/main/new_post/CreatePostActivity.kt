package com.vladima.cursandroid.ui.main.new_post

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.vladima.cursandroid.R
import com.vladima.cursandroid.Utils
import com.vladima.cursandroid.databinding.ActivityCreatePostBinding
import com.vladima.cursandroid.models.DbUserPost
import com.vladima.cursandroid.models.UserPost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private var imageFile: File? = null
    private var capturedPhoto = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        savedInstanceState?.let {
            capturedPhoto = it.getBoolean("capturedPhoto")
            imageFile = it.getString("imageFilePath")?.let { it1 -> File(it1) }
        }

        if (!capturedPhoto) {
            createFileForPhoto()
            capturedPhoto = true
        } else {
            setImageView()
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss")
        val current = LocalDateTime.now().format(formatter)

        val auth = FirebaseAuth.getInstance()
        val storageRef = FirebaseStorage.getInstance().reference.child("${auth.currentUser!!.uid}/$current")

        binding.createPostBtn.setOnClickListener {
            if (binding.descriptionEdt.text.toString().isEmpty()) {
                binding.description.helperText = "Please enter description first"
                return@setOnClickListener
            }
            binding.description.helperText = ""
            binding.progressBar.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                storageRef.putFile(getFinalFileUri(imageFile!!)).addOnSuccessListener {

                    val userPostCollection = Firebase.firestore.collection("userPosts")
                    val currentUser = auth.currentUser!!
                    userPostCollection.add(
                        DbUserPost(
                            current,
                            currentUser.uid,
                            binding.descriptionEdt.text.toString()
                        )
                    )
                    imageFile!!.delete()
                    Toast.makeText(this@CreatePostActivity, getString(R.string.post_success), Toast.LENGTH_SHORT).show()
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this@CreatePostActivity, getString(R.string.post_error), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("capturedPhoto", capturedPhoto)
        imageFile?.let {
            outState.putString("imageFilePath", it.absolutePath)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createFileForPhoto() {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss")
        val current = LocalDateTime.now().format(formatter)
        imageFile = File(filesDir, current)
        imageFile!!.createNewFile()
        getImageActivityResult.launch(
            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", imageFile!!)
                )
                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        )
    }

    private val getImageActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        with(result) {
            if (resultCode == RESULT_OK) {
                setImageView()
            } else {
                Toast.makeText(this@CreatePostActivity, getString(R.string.action_cancelled), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setImageView() {
        try {
            binding.newPostImage.setImageBitmap(Utils.rotateImage(BitmapFactory.decodeFile(imageFile!!.path), 90f))
        } catch (e: Exception) {
            binding.newPostImage.setImageBitmap(BitmapFactory.decodeFile(imageFile!!.path))
        }
    }

    private fun getFinalFileUri(file: File): Uri {

        var bitmap = BitmapFactory.decodeFile(imageFile!!.path)
        bitmap = Utils.rotateImage(bitmap, 90f)

        val byteStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteStream)
        val bitData = byteStream.toByteArray()

        val fileOutput = FileOutputStream(file)
        with(fileOutput) {
            write(bitData)
            flush()
            close()
        }

        return Uri.fromFile(file)
    }
}