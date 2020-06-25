package com.dgdevelop.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var  firebaseAuth : FirebaseAuth
    private lateinit var authListener: FirebaseAuth.AuthStateListener
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var  callbackManager: CallbackManager

    private val MainInfo = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Facebook
        //FacebookSdk.sdkInitialize(getApplicationContext()); // Esto ya no
        //AppEventsLogger.activateApp(this); // Esto ya no
        callbackManager = CallbackManager.Factory.create()

        initialize()

        btnCreateAccount.setOnClickListener {
            createAccount(etEmail.text.toString(), etPassword.text.toString() )
        }

        btnCreateSignIn.setOnClickListener {
            signIn(etEmail.text.toString(), etPassword.text.toString())
        }

        btnSignInGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        btnSignInFacebook.setReadPermissions("email", "public_profile")
        btnSignInFacebook.registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult?) {
                Log.w(TAG, "Facebook Login Success Token: ${result?.accessToken?.token}")
                firebaseAuthWithFacebook(result!!.accessToken)
            }

            override fun onCancel() {
                Log.w(TAG, "Facebook Cancel")
            }

            override fun onError(error: FacebookException?) {
                Log.w(TAG, "Facebook Error: ${error?.message}")
            }
        })

    }

    private fun initialize(){
        firebaseAuth = FirebaseAuth.getInstance()
        //val currentUser = firebaseAuth.currentUser
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null){
                Log.w(MainInfo, "onAuthStateChanged - signed_in ${firebaseUser.uid}")
                Log.w(MainInfo, "onAuthStateChanged - signed_in ${firebaseUser.email}")
            }else{
                Log.w(MainInfo, "onAuthStateChanged - signed_out")
            }
        }

        //Inicialización de Google Account
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

    }

    private fun createAccount(email: String, password: String){
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    Toast.makeText(this, "Create Account Success", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, "Error to Create account", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signIn(email: String, password: String){
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    Toast.makeText(this, "Authentication Success", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, WelcomeActivity::class.java))
                    finish()
                }else{
                    Toast.makeText(this, "Don't can authentication", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    Toast.makeText(this, "Google Authentication Success", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, WelcomeActivity::class.java))
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Google Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun firebaseAuthWithFacebook(accessToken: AccessToken){
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener {task ->
                if(task.isSuccessful){
                    Log.d(TAG, "signInWithCredential:success")
                    Toast.makeText(this, "Facebook Authentication Success", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, WelcomeActivity::class.java))
                    finish()
                }else{
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Facebook Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(authListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(authListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)

            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        }else{
            //Facebook
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }
}