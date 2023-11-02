package com.jfo1g21.android.projectmanagement

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.net.URL


class RegisterFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    private val defaultImage = "https://picsum.photos/200/200"
    private lateinit var email: EditText
    private lateinit var passwd: EditText
    private lateinit var passmatch: EditText
    private lateinit var skills: EditText
    private lateinit var name: EditText
    private lateinit var profileImage: Button
    private lateinit var registerButton: Button
    private lateinit var imageURL: EditText
    private lateinit var imagePreview: ImageView

    //TODO: fix callback
//    interface RegisterCallbacks {
//        fun signOut()
//    }
//
//    private var registerCallbacks: RegisterCallbacks? = null
//
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        registerCallbacks = context as RegisterCallbacks?
//    }
//
//    override fun onDetach() {
//        super.onDetach()
//        registerCallbacks = null
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        auth = Firebase.auth
        name = view.findViewById(R.id.full_name) as EditText
        email = view.findViewById(R.id.email_text) as EditText
        passwd = view.findViewById(R.id.pass_text) as EditText
        passmatch = view.findViewById(R.id.passmatch_text) as EditText
        skills = view.findViewById(R.id.skill_text) as EditText
        profileImage = view.findViewById(R.id.image_button) as Button
        registerButton = view.findViewById(R.id.create_button) as Button
        imageURL = view.findViewById(R.id.image_url) as EditText
        imagePreview = view.findViewById(R.id.image_view) as ImageView

        profileImage.setOnClickListener {
            try {
                val policy = ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)
                val url = URL(imageURL.text.toString())
                //TODO FIX
                val bmp = BitmapFactory.decodeStream(url.openStream())
              //  Log.d("bitmap", bmp.toString())
             imagePreview.setImageBitmap(bmp)


            }

            catch (e: Exception) {
                e.printStackTrace()
            }


        }

        registerButton.setOnClickListener {
            val nameString = name.text.toString()
            val emailString = email.text.toString()
            val passwordString = passwd.text.toString()
            val passmatchString = passmatch.text.toString()
            if (nameString.isNotEmpty() && emailString.isNotEmpty() && passwordString.isNotEmpty() && passmatchString.isNotEmpty()) {
                val image = if(imageURL.text.toString() != null)  imageURL.text.toString() else defaultImage

                //TODO finish getting skills and image to send to db during create account
                val newLine = System.getProperty("line.separator")
                val skillList: List<String> = skills.text.toString().split(newLine)
                //Log.d("skills list", skillsText.toString())
                if (passwordString == passmatchString) {
                    createAccount(nameString, emailString, passwordString, image, skillList)
                }
            }
        }

        return view
    }


    private fun createAccount(
        name: String,
        email: String,
        password: String,
        image: String,
        skills: List<String>
    ) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    //TODO ADD STUFFS TO DB

//                    Log.d("test", DH.test().toString())

                    sendEmailVerification()
                    val database = DatabaseHandler
                    Log.d("test", user?.uid.toString())
                    database.createProfile(name, email, image, skills, this)

                } else {
                    Log.d(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        context, "Registration failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
            }

    }


    private fun sendEmailVerification() {

        val user = auth.currentUser!!
        user.sendEmailVerification()
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    //TODO FIX causes crash
//                    Toast.makeText(
//                        context, "Verification Sent to Email.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    Log.d(TAG, "Email sent.")
                }

            }

    }

    private fun reload() {

    }

    public fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            Firebase.auth.signOut()
            //registerCallbacks?.signOut()
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()


        } else {
            reload()
        }
    }

    companion object {
        private const val TAG = "Register User"
    }
}