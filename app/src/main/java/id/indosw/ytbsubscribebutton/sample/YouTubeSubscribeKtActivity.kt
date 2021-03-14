@file:Suppress("DEPRECATION")

package id.indosw.ytbsubscribebutton.sample

import android.Manifest
import android.accounts.AccountManager
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.Scope
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerFragment
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTubeScopes
import id.indosw.ytbsubscribebutton.lib.YouTubeActivityPresenter
import id.indosw.ytbsubscribebutton.lib.YouTubeActivityView
import id.indosw.ytbsubscribebutton.lib.easypermissions.EasyPermissions
import id.indosw.ytbsubscribebutton.lib.easypermissions.EasyPermissions.PermissionCallbacks

class YouTubeSubscribeKtActivity : AppCompatActivity(), YouTubePlayer.OnInitializedListener,
    PermissionCallbacks, YouTubeActivityView {
    private val youtubeKey = "AIzaSyA-F-SoBTgtZr-em965Po5Wzw_Upxrf4U8"
    private var mCredential: GoogleAccountCredential? = null
    private var pDialog: ProgressDialog? = null
    private var presenter: YouTubeActivityPresenter? = null
    private var counter = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_you_tube_subscribe)

        // initialize presenter
        presenter = YouTubeActivityPresenter(this, resources.getString(R.string.app_name))
        val emailId = intent.extras!!.getString(GoogleSignInActivity.USER_EMAIL)
        val supportFragment =
            fragmentManager.findFragmentById(R.id.playerView) as YouTubePlayerFragment
        supportFragment.initialize(youtubeKey, this) // paste your youtube key
        mCredential = GoogleAccountCredential.usingOAuth2(
            applicationContext, listOf(YouTubeScopes.YOUTUBE)
        )
            .setBackOff(ExponentialBackOff())
        findViewById<View>(R.id.B_subscribe).setOnClickListener {
            /*FIRST GOTO FOLLOWING LINK AND ENABLE THE YOUTUBE API ACCESS
            https://console.developers.google.com/apis/api/youtube.googleapis.com/overview?project=YOUR_PROJECT_ID**/
            val settings = getPreferences(MODE_PRIVATE)
            val editor = settings.edit()
            editor.putString(PREF_ACCOUNT_NAME, emailId)
            editor.apply()
            resultsFromApi
        }
    }

    override fun onInitializationSuccess(
        provider: YouTubePlayer.Provider,
        youTubePlayer: YouTubePlayer,
        wasRestored: Boolean
    ) {
        if (!wasRestored) {
            // paste youtube video id here
            youTubePlayer.cueVideo("cHEahGHseGc") //Use cueVideo()  method, if you don't want to play it automatically
            //youTubePlayer.loadVideo("cHEahGHseGc"); //loadVideo() will auto play video
            // Hiding seek player controls
            youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL)
        }
    }

    override fun onInitializationFailure(
        provider: YouTubePlayer.Provider,
        youTubeInitializationResult: YouTubeInitializationResult
    ) {
        if (youTubeInitializationResult.isUserRecoverableError) {
            youTubeInitializationResult.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show()
        } else {
            val errorMessage = "Error: $youTubeInitializationResult"
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    private val youTubePlayerProvider: YouTubePlayer.Provider
        get() = findViewById(R.id.playerView)

    // handing subscribe task by presenter
    // pass youtube channelId as second parameter
    private val resultsFromApi: Unit
        get() {
            if (!isGooglePlayServicesAvailable) {
                acquireGooglePlayServices()
            } else if (mCredential!!.selectedAccountName == null) {
                chooseAccount()
            } else {
                pDialog = ProgressDialog(this@YouTubeSubscribeKtActivity)
                pDialog!!.setMessage("Please wait...")
                pDialog!!.show()
                // handing subscribe task by presenter
                presenter!!.subscribeToYouTubeChannel(
                    mCredential,
                    "UC_x5XG1OV2P6uZZ5FSM9Ttw"
                ) // pass youtube channelId as second parameter
            }
        }

    // checking google play service is available on phone or not
    private val isGooglePlayServicesAvailable: Boolean
        get() {
            val apiAvailability = GoogleApiAvailability.getInstance()
            val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
            return connectionStatusCode == ConnectionResult.SUCCESS
        }

    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            val dialog = apiAvailability.getErrorDialog(
                this@YouTubeSubscribeKtActivity,  // showing dialog to user for getting google play service
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES
            )
            dialog.show()
        }
    }

    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            val accountName = getPreferences(MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null)
            if (accountName != null) {
                mCredential!!.selectedAccountName = accountName
                resultsFromApi
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                    mCredential!!.newChooseAccountIntent(),
                    REQUEST_ACCOUNT_PICKER
                )
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                this,
                "This app needs to access your Google account for YouTube channel subscription.",
                REQUEST_PERMISSION_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RECOVERY_DIALOG_REQUEST -> youTubePlayerProvider.initialize(youtubeKey, this)
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != RESULT_OK) {
                Toast.makeText(
                    this,
                    "This app requires Google Play Services. Please " +
                            "install Google Play Services on your device and relaunch this app.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                resultsFromApi
            }
            REQUEST_ACCOUNT_PICKER -> if (resultCode == RESULT_OK && data != null && data.extras != null) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    val settings = getPreferences(MODE_PRIVATE)
                    val editor = settings.edit()
                    editor.putString(PREF_ACCOUNT_NAME, accountName)
                    editor.apply()
                    mCredential!!.selectedAccountName = accountName
                    resultsFromApi
                }
            }
            REQUEST_AUTHORIZATION -> if (resultCode == RESULT_OK) {
                resultsFromApi
            }
            RC_SIGN_IN -> {
                val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
                if (result != null) {
                    if (result.isSuccess) {
                        resultsFromApi
                    } else {
                        Toast.makeText(
                            this,
                            "Permission Required if granted then check internet connection",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        resultsFromApi // user have granted permission so continue
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Toast.makeText(
            this,
            "This app needs to access your Google account for YouTube channel subscription.",
            Toast.LENGTH_SHORT
        ).show()
    }

    // responce from presenter on success
    override fun onSubscribetionSuccess(title: String) {
        if (pDialog != null && pDialog!!.isShowing) {
            pDialog!!.dismiss()
        }
        Toast.makeText(
            this@YouTubeSubscribeKtActivity,
            "Successfully subscribe to $title",
            Toast.LENGTH_SHORT
        ).show()
    }

    // responce from presenter on failure
    override fun onSubscribetionFail() {
        if (pDialog != null && pDialog!!.isShowing) {
            pDialog!!.dismiss()
        }
        // user don't have youtube channel subscribe permission so grant it form him
        // as we have not taken at the time of sign in
        if (counter < 3) {
            counter++ // attempt three times on failure
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope("https://www.googleapis.com/auth/youtube")) // require this scope for youtube channel subscribe
                .build()
            val googleApiClient = GoogleSignIn.getClient(this, gso)
            val signInIntent = googleApiClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        } else {
            Toast.makeText(
                this, """
     goto following link and enable the youtube api access
     https://console.developers.google.com/apis/api/youtube.googleapis.com/overview?project=YOUR_PROJECT_ID
     """.trimIndent(),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    companion object {
        // if you are using YouTubePlayerView in xml then activity must extend YouTubeBaseActivity
        private const val RECOVERY_DIALOG_REQUEST = 1
        const val REQUEST_ACCOUNT_PICKER = 1000
        const val REQUEST_AUTHORIZATION = 1001
        const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
        private const val RC_SIGN_IN = 12311
        private const val PREF_ACCOUNT_NAME = "accountName"
    }
}