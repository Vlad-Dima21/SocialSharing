package com.vladima.cursandroid.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vladima.cursandroid.R
import com.vladima.cursandroid.databinding.FragmentHomeBinding
import com.vladima.cursandroid.databinding.FragmentSettingsBinding
import com.vladima.cursandroid.models.User
import com.vladima.cursandroid.ui.authentication.AuthenticateActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        (activity as AppCompatActivity).supportActionBar?.let {
            it.show()
            it.title = getString(R.string.settings)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val auth = FirebaseAuth.getInstance()
        val usersCollection = Firebase.firestore.collection("users")

        CoroutineScope(Dispatchers.IO).launch {
            val currentUser = usersCollection.whereEqualTo("userUID", auth.currentUser!!.uid).get().await().documents[0].toObject(
                User::class.java)
            withContext(Dispatchers.Main) {
                binding.homeText.text = "Hello ${currentUser?.userName}"
            }
        }

        binding.logoutBtn.setOnClickListener {
            auth.signOut()
            activity?.startActivity(Intent(activity, AuthenticateActivity::class.java))
            activity?.finish()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}