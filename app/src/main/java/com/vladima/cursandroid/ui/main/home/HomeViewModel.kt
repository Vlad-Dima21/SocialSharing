package com.vladima.cursandroid.ui.main.home

import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.vladima.cursandroid.models.User
import com.vladima.cursandroid.models.UserPost
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
//    private val storageRef = FirebaseStorage.getInstance().reference.child("${auth.currentUser!!.uid}/")
    private val storageRef = FirebaseStorage.getInstance().reference.child("${auth.currentUser!!.uid}")

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()
    private val _userPosts = MutableStateFlow(listOf<UserPost>())
    val userPosts = _userPosts.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val tempFiles = mutableListOf<File>()

    init {
        loadCurrentUserPosts()
    }

    fun loadCurrentUserPosts() = CoroutineScope(Dispatchers.IO).launch {
        _isLoading.emit(true)
        val imageRefs = storageRef.listAll().await()
        val userPosts = mutableListOf<UserPost>()
        val jobs = mutableListOf<Job>()
        imageRefs.items.forEachIndexed { index, storageReference ->
            jobs.add(
                launch(Dispatchers.IO) {
//                    val localFile = File.createTempFile("${auth.currentUser!!.uid}_$index", "jpg")
                    val localFile = File.createTempFile(storageReference.name, "jpg")
                    tempFiles.add(localFile)
                    storageReference.getFile(localFile).await()
                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                    userPosts.add(UserPost(bitmap!!, storageReference.name))
                }
            )
        }
        jobs.forEach {
            it.join()
        }
        userPosts.sortBy { it.imageDescription }
        _userPosts.emit(userPosts)
        _isLoading.emit(false)
        clearCache()
    }

    fun clearCache() = tempFiles.forEach {
        it.delete()
    }
}