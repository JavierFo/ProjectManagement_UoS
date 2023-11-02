package com.jfo1g21.android.projectmanagement

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.*
import android.webkit.URLUtil
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.net.URL


/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private val defaultImage = "https://picsum.photos/200/200"
    private lateinit var email: TextView
    private lateinit var name: TextView
    private lateinit var skills: EditText
    private lateinit var imageURL: EditText
    private lateinit var profileImage: ImageView
    private lateinit var updateButton: Button
    private lateinit var imageButton: Button
    private lateinit var skillArray: ArrayList<String>

    //TODO: add menu to profile page
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_profile_projects, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.view_pending_projects -> {
                true
            }
            R.id.view_current_projects -> {
               true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        auth = Firebase.auth
        val user = auth.currentUser
        val userData = DatabaseHandler.getIndividualUser(user?.uid.toString())

        // Log.d(TAG, user.toString())
        email = view.findViewById(R.id.email_confirm) as TextView
        name = view.findViewById(R.id.full_name_view) as TextView
        skills = view.findViewById(R.id.skill_update) as EditText
        imageURL = view.findViewById(R.id.image_url_update) as EditText
        profileImage = view.findViewById(R.id.image_update) as ImageView
        updateButton = view.findViewById(R.id.update_button) as Button
        imageButton = view.findViewById(R.id.image_button_update) as Button



        email.text = userData?.get("email").toString()
        name.text = userData?.get("name").toString()
        // skills.setText(userData?.get("skills").toString())
        imageURL.setText(userData?.get("image").toString())

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        getImageByURL(userData?.get("image").toString())

        val newLine = System.getProperty("line.separator")
        skillArray = userData?.get("skills") as ArrayList<String>
        //TODO fix

        var skill = ""
        for (i in skillArray.indices) {
            skill += skillArray[i]
            if (i != skillArray.size - 1) {
                skill += newLine
            }
        }
        //Log.d("skill",kill)
        skills.hint = skill


        // skills.setText(userData?.get("skills").toString())


        imageButton.setOnClickListener {

            getImageByURL(imageURL.text.toString())
//            val url = URL(imageURL.text.toString())
//            val bmp = BitmapFactory.decodeStream(url.openStream())
//            profileImage.setImageBitmap(bmp)

        }


        updateButton.setOnClickListener {
            val emailString = email.text.toString()
            val nameString = name.text.toString()
            val imageString = imageURL.text.toString()
            val newLine = System.getProperty("line.separator")
            val skillsText: List<String> = skills.text.toString().split(newLine)
            updateAccount(nameString, emailString, imageString, skillsText)
        }

        return view
    }

    private fun updateAccount(
        name: String,
        email: String,
        image: String,
        skills: List<String>
    ) {
        Log.d("update", "updateUserProfile:success")
        DatabaseHandler.updateProfile(name, email, image, skills)
        Toast.makeText(
            context, "Profile updated.",
            Toast.LENGTH_SHORT
        ).show()

    }


    fun getImageByURL(image: String) {
        var bmp: Bitmap
        try {
            Log.d("imag",  URLUtil.isValidUrl(image).toString())
            //TODO: CRASHES ON INVALID URL
            bmp = BitmapFactory.decodeStream(URL(image).openStream())
        } catch (e: FileNotFoundException) {
            Toast.makeText(
                context, "Image Doesnt exist using random image.",
                Toast.LENGTH_SHORT
            ).show()
            bmp = BitmapFactory.decodeStream(URL(defaultImage).openStream())

        }
        profileImage.setImageBitmap(bmp)
    }

    fun sendInviteToProject(email: String) {
//        val sender = GMailSender(
//            "you_email@gmail.com",
//            "password"
//        )
//
//        sender.sendMail(subject, message, sender, recipients)
    }

    companion object {
        private const val TAG = "Profile Update"
    }
}