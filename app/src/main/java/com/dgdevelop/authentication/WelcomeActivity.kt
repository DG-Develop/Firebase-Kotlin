package com.dgdevelop.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {
    private lateinit var  firebaseAuth : FirebaseAuth
    private lateinit var authListener: FirebaseAuth.AuthStateListener
    private lateinit var googleSignInClient: GoogleSignInClient

    private val Welcome = "WelcomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        initialize()

        signOut.setOnClickListener {
            signOut()
        }
    }

    private fun signOut(){
        firebaseAuth.signOut()

        if(googleSignInClient != null){
            googleSignInClient.signOut().addOnCompleteListener(this) {task ->
                if(task.isSuccessful){
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }else{
                    Toast.makeText(this, "Error to sign out", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Facebook
        if(LoginManager.getInstance() != null){
            LoginManager.getInstance().logOut()
        }

    }

    private fun initialize(){
        firebaseAuth = FirebaseAuth.getInstance()
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null){
                tvUserDetail.text = "IDUser: ${firebaseUser.uid} \nEmail: ${firebaseUser.email}"
                Picasso
                    .get()
                    .load(firebaseUser.photoUrl)
                    .into(ivPhoto)
            }else{
                Log.w(Welcome, "onAuthStateChanged - signed_out")
            }
        }

        //Inicialización de Google Account
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(authListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(authListener)
    }
}