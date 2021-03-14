package id.indosw.ytbsubscribebutton.sample

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class GoogleSignInActivityKt : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()

        // Configure Google Sign In option
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail() //.requestScopes(new Scope("https://www.googleapis.com/auth/youtube"))
            // you can request scope here OR at the time of subscribe
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        findViewById<View>(R.id.B_subscribe).setOnClickListener { signIn() }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and send to next activity accordingly.
        val currentUser = mAuth!!.currentUser
        if (currentUser != null) {
            startActivity(
                Intent(this, YouTubeSubscribeKtActivity::class.java).putExtra(
                    USER_EMAIL,
                    currentUser.email
                )
            )
            finish()
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result != null) {
                if (result.isSuccess) {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = result.signInAccount
                    account?.let { firebaseAuthWithGoogle(it) }
                } else {
                    // check internet connection, display a message to the user.
                    Toast.makeText(this, "Check your internet", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = mAuth!!.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        this@GoogleSignInActivityKt,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            startActivity(
                Intent(this, YouTubeSubscribeKtActivity::class.java).putExtra(
                    USER_EMAIL,
                    currentUser.email
                )
            )
            finish()
        }
    }

    companion object {
        private const val RC_SIGN_IN = 1212
        private const val USER_EMAIL = "userEmailId"
    }
}