package com.vladima.cursandroid.ui.authentication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vladima.cursandroid.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthenticateViewModel: ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val usersCollection = Firebase.firestore.collection("users")

    var email by mutableStateOf("")
    var userName by mutableStateOf("")
    var password by mutableStateOf("")

    var authenticationMethod by mutableStateOf(0)

    private val _errorMsg = MutableStateFlow("")
    val errorMsg = _errorMsg.asStateFlow()
    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    private val fieldsValidation
        get() = when (authenticationMethod) {
            0 -> email.isNotEmpty() && password.isNotEmpty() && userName.isNotEmpty()
            else -> email.isNotEmpty() && password.isNotEmpty()
        }


    fun signUp() = CoroutineScope(Dispatchers.IO).launch {
        if (!fieldsValidation) {
            updateMessage("Please fill required fields")
            return@launch
        }
        try {
            auth.createUserWithEmailAndPassword(email, password).await()
        } catch (e: Exception) {
            updateMessage(e.message ?: "There was an error")
            return@launch
        }
        if (auth.currentUser == null) {
            updateMessage("Couldn't sign up. Please try again")
            return@launch
        }
        usersCollection.add(User(auth.currentUser!!.uid, userName)).await()
        _isSuccess.emit(true)
    }

    fun logIn() = CoroutineScope(Dispatchers.IO).launch {
        if (!fieldsValidation) {
            updateMessage("Please fill required fields")
            return@launch
        }
        try {
            auth.signInWithEmailAndPassword(email, password).await()
        } catch (e: Exception) {
            updateMessage(e.message ?: "There was an error")
            return@launch
        }
        if (auth.currentUser == null) {
            updateMessage("Couldn't log in. Please try again")
            return@launch
        }
        _isSuccess.emit(true)
    }

    private suspend fun updateMessage(msg: String) {
        _errorMsg.emit(msg)
        delay(500)
        _errorMsg.emit("")
    }
}