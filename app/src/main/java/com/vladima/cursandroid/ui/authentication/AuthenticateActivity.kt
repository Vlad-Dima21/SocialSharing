package com.vladima.cursandroid.ui.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vladima.cursandroid.R
import com.vladima.cursandroid.models.User
import com.vladima.cursandroid.ui.main.MainActivity
import com.vladima.cursandroid.ui.theme.SocialSharingTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthenticateActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            startMainActivity()
        }

        val viewModel: AuthenticateViewModel by viewModels()

        lifecycleScope.launch {
            viewModel.errorMsg.collect { msg ->
                if (msg != null) {
                    Toast.makeText(this@AuthenticateActivity, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
        lifecycleScope.launch {
            viewModel.isSuccess.collect { isSuccess ->
                if (isSuccess) {
                    Toast.makeText(this@AuthenticateActivity, getString(R.string.user_logged_in), Toast.LENGTH_SHORT).show()
                    startMainActivity()
                }
            }
        }

        setContent {
            SocialSharingTheme {
                val errorMsg by viewModel.errorMsg.collectAsState()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background)
                        .padding(32.dp)
                ) {
                    AnimatedVisibility(
                        visible = viewModel.authenticationMethod == 0,
                        modifier = Modifier.align(Alignment.Center),
                        enter = slideInHorizontally() + fadeIn(),
                        exit = fadeOut() + slideOutHorizontally()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            Text(
                                text = stringResource(id = R.string.app_name),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(64.dp))
                            OutlinedTextField(
                                label = {
                                    Text(getString(R.string.email))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                value = viewModel.email,
                                onValueChange = { viewModel.email = it },
                                shape = RoundedCornerShape(dimensionResource(id = R.dimen.roundedCornerShape)),
                                isError = listOf(R.string.WrongEmail, R.string.FieldsNotFilled).contains(errorMsg)
                            )
                            OutlinedTextField(
                                label = {
                                    Text(getString(R.string.user_name))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                                value = viewModel.userName,
                                onValueChange = { viewModel.userName = it },
                                shape = RoundedCornerShape(dimensionResource(id = R.dimen.roundedCornerShape)),
                                isError = errorMsg == R.string.FieldsNotFilled
                            )
                            OutlinedTextField(
                                label = {
                                    Text(stringResource(id = R.string.password))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                visualTransformation = PasswordVisualTransformation(),
                                value = viewModel.password,
                                onValueChange = { viewModel.password = it },
                                shape = RoundedCornerShape(dimensionResource(id = R.dimen.roundedCornerShape)),
                                isError = errorMsg == R.string.FieldsNotFilled
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(onClick = viewModel::signUp, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Filled.Login,
                                        contentDescription = null,
                                        modifier = Modifier.padding(ButtonDefaults.IconSpacing)
                                    )
                                    Text(
                                        text = getString(R.string.sign_up),
                                        fontSize = 24.sp,
                                        modifier = Modifier.padding(ButtonDefaults.IconSpacing)
                                    )
                                }
                                Button(onClick = ::loginGoogle, modifier = Modifier.weight(0.5f)) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.gugal),
                                        contentDescription = null,
                                        modifier = Modifier.padding(ButtonDefaults.IconSpacing)
                                    )
                                }
                            }
                            Row {
                               Text(
                                   modifier = Modifier.padding(top = ButtonDefaults.TextButtonContentPadding.calculateTopPadding() + ButtonDefaults.ContentPadding.calculateTopPadding() - 3.dp),
                                   text = getString(R.string.already_account),
                                   color = MaterialTheme.colorScheme.onBackground
                               )
                               TextButton(onClick = {
                                   viewModel.apply {
                                       email = ""
                                       userName = ""
                                       password = ""
                                   }
                                   viewModel.authenticationMethod = 1
                               }) {
                                   Text(getString(R.string.login_instead))
                               }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = viewModel.authenticationMethod == 1,
                        modifier = Modifier.align(Alignment.Center),
                        enter = slideInHorizontally() + fadeIn(),
                        exit = fadeOut() + slideOutHorizontally()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            Text(
                                text = stringResource(id = R.string.app_name),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(64.dp))
                            OutlinedTextField(
                                label = {
                                    Text(getString(R.string.email))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                value = viewModel.email,
                                onValueChange = { viewModel.email = it },
                                shape = RoundedCornerShape(dimensionResource(id = R.dimen.roundedCornerShape)),
                                isError = listOf(R.string.WrongEmail, R.string.FieldsNotFilled).contains(errorMsg)
                            )
                            OutlinedTextField(
                                label = {
                                    Text(getString(R.string.password))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                visualTransformation = PasswordVisualTransformation(),
                                value = viewModel.password,
                                onValueChange = { viewModel.password = it },
                                shape = RoundedCornerShape(dimensionResource(id = R.dimen.roundedCornerShape)),
                                isError = errorMsg == R.string.FieldsNotFilled
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(onClick = viewModel::logIn, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Filled.Login,
                                        contentDescription = null,
                                        modifier = Modifier.padding(ButtonDefaults.IconSpacing)
                                    )
                                    Text(
                                        text = getString(R.string.login),
                                        fontSize = 24.sp,
                                        modifier = Modifier.padding(ButtonDefaults.IconSpacing)
                                    )
                                }
                                Button(onClick = ::loginGoogle, modifier = Modifier.weight(0.5f)) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.gugal),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(ButtonDefaults.IconSpacing)
                                    )
                                }
                            }
                            Row {
                                Text(
                                    modifier = Modifier.padding(top = ButtonDefaults.TextButtonContentPadding.calculateTopPadding() + ButtonDefaults.ContentPadding.calculateTopPadding() - 3.dp),
                                    text = getString(R.string.create_account),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                TextButton(onClick = {
                                    viewModel.apply {
                                        email = ""
                                        userName = ""
                                        password = ""
                                    }
                                    viewModel.authenticationMethod = 0
                                }) {
                                    Text(getString(R.string.sign_up_instead))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun loginGoogle() {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        val signInClient = GoogleSignIn.getClient(this, options)
        signInClient.signInIntent.also {
            googleSignInResult.launch(it)
        }
    }

    private val googleSignInResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            CoroutineScope(Dispatchers.IO).launch {
                val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).await()
                account?.let {
                    withContext(Dispatchers.Main) {
                        googleAuthForFirebase(it)
                    }
                }
            }
        } else {
            Toast.makeText(this, "Google authentication failed", Toast.LENGTH_LONG).show()
        }
    }

    private fun googleAuthForFirebase(account: GoogleSignInAccount) {
        val auth = FirebaseAuth.getInstance()
        val usersCollection = Firebase.firestore.collection("users")
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.signInWithCredential(credentials).await()
                if (usersCollection.whereEqualTo("userUID", auth.currentUser!!.uid).get().await().documents.isEmpty()) {
                    GoogleSignIn.getLastSignedInAccount(this@AuthenticateActivity)!!
                        .let { account ->
                            usersCollection.add(
                                User(
                                    auth.currentUser!!.uid,
                                    account.displayName ?: ""
                                )
                            ).await()
                        }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AuthenticateActivity, "Google authentication succeeded", Toast.LENGTH_LONG).show()
                    startMainActivity()
                }
            } catch(e: Exception) {
                Log.e("GOOGLE_ERROR", e.stackTraceToString())
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AuthenticateActivity, "Google authentication failed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}