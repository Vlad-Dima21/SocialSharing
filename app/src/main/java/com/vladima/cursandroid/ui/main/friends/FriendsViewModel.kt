package com.vladima.cursandroid.ui.main.friends

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.vladima.cursandroid.R
import com.vladima.cursandroid.models.User
import com.vladima.cursandroid.ui.main.MainActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val app: Application,
    private val notificationBuilder: NotificationCompat.Builder,
    private val notificationManager: NotificationManagerCompat
) : ViewModel() {

    private val authUser = FirebaseAuth.getInstance().currentUser!!
    private val usersCollection = Firebase.firestore.collection("users")
    private lateinit var currentUser: User
    private lateinit var currentUserDoc: DocumentSnapshot

    private var friendsList: List<String>? = null

    private val tempFiles = mutableListOf<File>()

    private lateinit var potentialFriendDoc: DocumentSnapshot
    private val _potentialFriendName = MutableStateFlow("")
    val potentialFriendName = _potentialFriendName.asStateFlow()

    init {
        usersCollection.whereEqualTo("userUID", authUser.uid).addSnapshotListener { value, error ->
            if (error == null) {
                currentUser = value!!.documents[0].toObject(User::class.java)!!
                currentUserDoc = value.documents[0]
                friendsList = if (friendsList == null) {
                    currentUser.friends
                } else {
                    currentUser.friends.find { !friendsList!!.contains(it) }
                        ?.let {
                            CoroutineScope(Dispatchers.IO).launch {
                                usersCollection.whereEqualTo("userUID", it).get().await().documents[0].toObject(User::class.java)?.let {
                                    newFriendNotification(it.userName)
                                }
                            }
                        }
                    currentUser.friends
                }
            } else {
                Toast.makeText(app, app.getString(R.string.friends_error), Toast.LENGTH_LONG).show()
                Log.e("FRIENDS_ERROR", error.stackTraceToString())
            }
        }
    }

    private fun newFriendNotification(newFriendName: String) {
        if (notificationManager.areNotificationsEnabled()) {
            val intent = Intent(app, MainActivity::class.java)
                .putExtra("friend_added", true)
            val pendingIntent: PendingIntent? = TaskStackBuilder.create(app).run {
                addNextIntent(intent)
                getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            notificationManager.notify(
                1,
                notificationBuilder.apply {
                    setContentTitle(app.getString(R.string.friend_notification_title))
                    setContentText("You are now friends with $newFriendName")
                    setContentText(
                        app.getString(
                            R.string.friend_notification_content,
                            newFriendName
                        )
                    )
                    setSmallIcon(R.drawable.baseline_people_24)
                    setContentIntent(pendingIntent)
                }.build()
            )
        }
    }

    fun alertNewFriend(userUID: String) = CoroutineScope(Dispatchers.IO).launch {
        currentUser = usersCollection.whereEqualTo("userUID", authUser.uid).get().await().documents[0].toObject(User::class.java)!!
        if (currentUser.userUID == userUID) {
            withContext(Dispatchers.Main) {
                Toast.makeText(app, app.getString(R.string.send_others), Toast.LENGTH_SHORT).show()
            }
            return@launch
        }
        if (currentUser.friends.contains(userUID)) {
            withContext(Dispatchers.Main) {
                Toast.makeText(app, app.getString(R.string.friend_already_added), Toast.LENGTH_SHORT).show()
            }
        } else {
            try {
                _potentialFriendName.emit(
                    usersCollection.whereEqualTo("userUID", userUID).get().await()
                        .run {
                            potentialFriendDoc = documents[0]
                            documents[0].toObject(User::class.java)!!.userName
                        })
            } catch (e: Exception) {
                Log.e("ADD_FRIEND_ERROR", e.stackTraceToString())
                withContext(Dispatchers.Main) {
                    Toast.makeText(app, app.getString(R.string.invite_error), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    fun addNewFriend() = CoroutineScope(Dispatchers.IO).launch {
        val newFriend = usersCollection.document(potentialFriendDoc.id).get().await()
            .toObject(User::class.java)!!
        usersCollection.document(potentialFriendDoc.id).update(mapOf(
            "friends" to newFriend.friends.toMutableList().apply { add(authUser.uid) }
        )).await()
        usersCollection.document(currentUserDoc.id).update(mapOf(
            "friends" to currentUser.friends.toMutableList().apply { add(newFriend.userUID) }
        )).await()
    }
}