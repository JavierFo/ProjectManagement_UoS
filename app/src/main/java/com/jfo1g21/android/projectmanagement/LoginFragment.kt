package com.jfo1g21.android.projectmanagement

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var email: EditText
    private lateinit var passwd: EditText
    private lateinit var loginButton: Button


    interface LoginCallbacks {
        fun userTokenReady(user: FirebaseUser)
    }

    private var loginCallbacks: LoginCallbacks? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        loginCallbacks = context as LoginCallbacks?
    }

    override fun onDetach() {
        super.onDetach()
        loginCallbacks = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        auth = Firebase.auth
        email = view.findViewById(R.id.email_login_text) as EditText
        passwd = view.findViewById(R.id.pass_login_text) as EditText
        loginButton = view.findViewById(R.id.login_btn) as Button

        loginButton.setOnClickListener {
            val emailString = email.text.toString()
            val passwordString = passwd.text.toString()
            if (emailString.isNotEmpty() && passwordString.isNotEmpty()) {
                logIn(emailString, passwordString)
            }
        }

        view.findViewById<Button>(R.id.signup_btn).setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, RegisterFragment())
                .commit()
        }

        view.findViewById<TextView>(R.id.reset_pass).setOnClickListener {
            resetPassword(email.text.toString())
        }
        return view

    }

    private fun logIn(email: String, password: String) {

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    if (user?.isEmailVerified != true) {
                        Firebase.auth.signOut()
                        Toast.makeText(
                            context, "Account Not Verified.",
                            Toast.LENGTH_SHORT
                        ).show()
                        updateUI(null)
                    } else {
                        DatabaseHandler.init(context)
                        updateUI(user)
                    }
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        context, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
            }
    }

    private fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        context, "Password Reset has been sent.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(TAG, "Email sent.")
                } else {
                    Toast.makeText(
                        context, "Failed to send a reset.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            loginCallbacks?.userTokenReady(user)
        } else {
            reload()
        }
    }

    private fun reload() {

    }

    companion object {
        private const val TAG = "Login Page"
    }
}