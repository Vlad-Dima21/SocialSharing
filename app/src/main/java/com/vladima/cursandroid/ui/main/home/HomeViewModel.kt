package com.vladima.cursandroid.ui.main.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.vladima.cursandroid.models.DbUserPost
import com.vladima.cursandroid.models.RVUserPost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

class HomeViewModel: ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference.child(auth.currentUser!!.uid)

    private val _userPosts = MutableStateFlow(listOf<RVUserPost>())
    val userPosts = _userPosts.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val tempFiles = mutableListOf<File>()

    private val currentUser = FirebaseAuth.getInstance().currentUser!!
    private val userPostsCollection = Firebase.firestore.collection("userPosts")

    init {
        loadCurrentUserPosts()
    }

    fun loadCurrentUserPosts() = CoroutineScope(Dispatchers.IO).launch {
        _isLoading.emit(true)

        val dbUserPosts = userPostsCollection.whereEqualTo("userUID", currentUser.uid).get().await().documents.map {
            it.toObject(DbUserPost::class.java)!!
        }

        val imageRefs = storageRef.listAll().await()
        val posts = mutableListOf<RVUserPost>()
        val jobs = mutableListOf<Job>()
        imageRefs.items.forEachIndexed { index, storageReference ->
            jobs.add(
                launch(Dispatchers.IO) {
                    val localFile = File.createTempFile(storageReference.name, "jpg")
                    tempFiles.add(localFile)
                    storageReference.getFile(localFile).await()
                    val fbBitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                    // codul de mai jos e pentru a reduce memoria consumata de imaginile din recycler view
                    val bitmap = Bitmap.createScaledBitmap(fbBitmap, fbBitmap.width / 2, fbBitmap.height / 2, false)
                    posts.add(RVUserPost(storageReference.name, bitmap!!, dbUserPosts.find { it.fileName == storageReference.name }?.description ?: storageReference.name))
                }
            )
        }
        jobs.forEach {
            it.join()
        }
        _userPosts.emit(
            posts.sortedByDescending { dbUserPosts.find { it2 -> it2.fileName == it.fileName }?.createDate }
        )
        _isLoading.emit(false)
        clearCache()
    }

    private fun clearCache() = tempFiles.forEach {
        it.delete()
    }
}