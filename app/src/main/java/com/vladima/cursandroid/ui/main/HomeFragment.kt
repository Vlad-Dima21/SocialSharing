package com.vladima.cursandroid.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vladima.cursandroid.R
import com.vladima.cursandroid.databinding.FragmentHomeBinding
import com.vladima.cursandroid.models.User
import com.vladima.cursandroid.ui.authentication.AuthenticateActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val auth = FirebaseAuth.getInstance()
        val usersCollection = Firebase.firestore.collection("users")

        binding = FragmentHomeBinding.inflate(layoutInflater)

        CoroutineScope(Dispatchers.IO).launch {
            val currentUser = usersCollection.whereEqualTo("userUID", auth.currentUser!!.uid).get().await().documents[0].toObject(User::class.java)
            withContext(Dispatchers.Main) {
                binding.homeText.text = "Hello ${currentUser?.userName}"
            }
        }

        binding.logoutBtn.setOnClickListener {
            auth.signOut()
            activity?.startActivity(Intent(activity, AuthenticateActivity::class.java))
            activity?.finish()
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}