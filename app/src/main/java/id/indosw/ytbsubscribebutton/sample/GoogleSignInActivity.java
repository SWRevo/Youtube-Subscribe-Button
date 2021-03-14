package id.indosw.ytbsubscribebutton.sample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

@SuppressWarnings("deprecation")
public class GoogleSignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1212;
    protected static final String USER_EMAIL = "userEmailId";
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In option
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                //.requestScopes(new Scope("https://www.googleapis.com/auth/youtube"))
                // you can request scope here OR at the time of subscribe
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        findViewById(R.id.B_subscribe).setOnClickListener(v -> signIn());
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and send to next activity accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            startActivity(new Intent(this,YouTubeSubscribeActivity.class).putExtra(USER_EMAIL,currentUser.getEmail()));
            finish();
        }

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result != null) {
                if (result.isSuccess())
                {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = result.getSignInAccount();
                    if (account != null) {
                        firebaseAuthWithGoogle(account);
                    }
                }
                else
                {
                    // check internet connection, display a message to the user.
                    Toast.makeText(this, "Check your internet", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful())
                    {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    }
                    else
                    {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(GoogleSignInActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }


    private void updateUI(FirebaseUser currentUser) {
        if(currentUser!=null){
            startActivity(new Intent(this,YouTubeSubscribeActivity.class).putExtra(USER_EMAIL,currentUser.getEmail()));
            finish();
        }
    }

}
