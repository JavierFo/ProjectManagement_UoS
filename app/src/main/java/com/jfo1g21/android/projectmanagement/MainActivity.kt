package com.jfo1g21.android.projectmanagement

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*
private const val TAG = "LoginUserToken"

class MainActivity : AppCompatActivity(), ProjectListFragment.Callbacks, LoginFragment.LoginCallbacks {

    private lateinit var auth: FirebaseAuth
    private var memberManagerViewBool: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        //Log.d("AUTH",auth.toString())
        //signOut() //TODO remove when logout button implemented
        setContentView(R.layout.activity_main)

        //TODO maybe fix ordering?
        if (auth.currentUser == null) {
            //val fragment = RegisterFragment()
            val fragment = LoginFragment()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        } else {
            val currentFragment =
                supportFragmentManager.findFragmentById(R.id.fragment_container)

            if (currentFragment == null) {
                val fragment = ProjectListFragment.newInstance()
                supportFragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit()
            }
        }
    }

    override fun onProjectSelected(currentUserId: String, PMUserId: String, projectID: String) {
        if (currentUserId != PMUserId) {
            val fragment = ProjectFragment()
            fragment.currentProjectID = projectID
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
                .addToBackStack(null).commit()
        } else {
            val fragment = TaskListFragment()
            fragment.currentProjectID = projectID
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
                .addToBackStack(null).commit()
        }
    }

    override fun createNewProjectSelected() {
        val fragment = NewProjectFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
            .addToBackStack(null).commit()
    }

    override fun viewProfile(){
        val fragment = ProfileFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
//            .add(R.id.fragment_container, fragment)
            .addToBackStack(null).commit()
    }

//    private fun checkCurrentUser() {
//
//        val user = Firebase.auth.currentUser
//        if (user != null) {
//            //TODO get edge case of account being deleted on different device but not updated locally on another
//            //onAuthStateChanged(FirebaseAuth auth)
//            getUserProfile()
//        } else {
//            //TODO go to signin page
//        }
//    }

//    private fun getUserProfile() {
//        val user = Firebase.auth.currentUser
//        user?.let {
//            // Name, email address, and profile photo Url
//            val name = user.displayName
//            val email = user.email
//            val photoUrl = user.photoUrl
//
//            val emailVerified = user.isEmailVerified
//
//            val uid = user.uid
//        }
//
//    }

    override fun signOut() {
        //  auth.signOut() {
        //TODO setup signout option with a profile icon
        Firebase.auth.signOut()
        //  }
    }

    override fun userTokenReady(user: FirebaseUser) {
//        val currentFragment =
//            supportFragmentManager.findFragmentById(R.id.fragment_container)

       // if (currentFragment == null) {
            val fragment = ProjectListFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()

    //    }
    }
}