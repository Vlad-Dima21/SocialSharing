package com.vladima.cursandroid.ui.authentication

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vladima.cursandroid.R
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

    private val _errorMsg = MutableStateFlow<Int?>(null)
    val errorMsg = _errorMsg.asStateFlow()
    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    private val emailRegex = Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$")

    private enum class Validations {
        WrongEmail,
        FieldsNotFilled
    }

    private val fieldsValidation
        get() = when (authenticationMethod) {
            0 -> {
                if (!emailRegex.matches(email)) {
                    Validations.WrongEmail
                } else if (email.isEmpty() || password.isEmpty() || userName.isEmpty()) {
                    Validations.FieldsNotFilled
                } else {
                    null
                }
            }
            else -> {
                if (!emailRegex.matches(email)) {
                    Validations.WrongEmail
                } else if (email.isEmpty() || password.isEmpty()) {
                    Validations.FieldsNotFilled
                } else {
                    null
                }
            }
        }


    fun signUp() = CoroutineScope(Dispatchers.IO).launch {
        when(fieldsValidation) {
            Validations.WrongEmail -> {
                updateMessage(R.string.WrongEmail)
                return@launch
            }
            Validations.FieldsNotFilled -> {
                updateMessage(R.string.FieldsNotFilled)
                return@launch
            }
            else -> {}
        }
        try {
            auth.createUserWithEmailAndPassword(email, password).await()
        } catch (e: FirebaseAuthUserCollisionException) {
            updateMessage(R.string.EmailAlreadyInUse)
            return@launch
        } catch (e: Exception) {
            updateMessage(R.string.Error)
            return@launch
        }
        if (auth.currentUser == null) {
            updateMessage(R.string.Error)
            return@launch
        }
        usersCollection.add(User(auth.currentUser!!.uid, userName)).await()
        _isSuccess.emit(true)
    }

    fun logIn() = CoroutineScope(Dispatchers.IO).launch {
        when(fieldsValidation) {
            Validations.WrongEmail -> {
                updateMessage(R.string.WrongEmail)
                return@launch
            }
            Validations.FieldsNotFilled -> {
                updateMessage(R.string.FieldsNotFilled)
                return@launch
            }
            else -> {}
        }
        try {
            auth.signInWithEmailAndPassword(email, password).await()
        } catch(e: FirebaseAuthInvalidCredentialsException) {
            updateMessage(R.string.InvalidCredentials)
            return@launch
        } catch (e: Exception) {
            updateMessage(R.string.Error)
            Log.d("Eroare", e.stackTraceToString())
            return@launch
        }
        if (auth.currentUser == null) {
            updateMessage(R.string.Error)
            return@launch
        }
        _isSuccess.emit(true)
    }

    private suspend fun updateMessage(msg: Int) {
        _errorMsg.emit(msg)
        delay(2000)
        _errorMsg.emit(null)
    }
}