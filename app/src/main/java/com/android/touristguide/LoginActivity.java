package com.android.touristguide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin;
    private GoogleSignInButton btnLoginGoogle;
    private Button btnForgotPassword;
    private Button btnSignUp;
    private EditText edEmail;
    private EditText edPassword;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 9001;
    private final String TAG = "LoginActivity";
    private AlertDialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLoginGoogle= (GoogleSignInButton) findViewById(R.id.btn_signin_google);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnForgotPassword = (Button) findViewById(R.id.btn_forgot_password);
        btnSignUp = (Button) findViewById(R.id.btn_to_sign_up);
        edEmail = (EditText) findViewById(R.id.ed_email_login);
        edPassword = (EditText) findViewById(R.id.ed_password_login);
        tilEmail = (TextInputLayout) findViewById(R.id.til_email_login);
        tilPassword = (TextInputLayout) findViewById(R.id.til_password_login);
        loadingDialog = Helper.createLoadingDialog(this);
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_client_id))
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions);

        btnLoginGoogle.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        btnLoginGoogle.setTextColor(0xFF000000);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signUpActivity = new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(signUpActivity);
            }
        });

        btnLoginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });

        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapActivity = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(mapActivity);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        edEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tilEmail.setError("");
            }
        });

        edPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tilPassword.setError("");
            }
        });
    }

    private void signInWithGoogle(){
        Intent signInWithGoogleIntent = mGoogleSignInClient.getSignInIntent();
        loadingDialog.show();
        startActivityForResult(signInWithGoogleIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                loadingDialog.cancel();
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, R.string.google_sign_in_failed, Toast.LENGTH_LONG).show();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            User user = new User(account.getDisplayName(),account.getEmail(),null,account.getPhotoUrl().toString());
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(currentUser.getUid())
                                    .setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            loadingDialog.cancel();
                                            if (task.isSuccessful()){
                                                Intent intent = new Intent(LoginActivity.this,MapActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }else{
                                                Log.d(TAG,"profile failed");
                                                Toast.makeText(LoginActivity.this, R.string.google_sign_in_failed, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.
                            loadingDialog.cancel();
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, R.string.google_sign_in_failed, Toast.LENGTH_LONG).show();
                        }

                        // ...
                    }
                });
    }

    private void login(){
        final String email = edEmail.getText().toString();
        boolean valid = true;
        if (email.isEmpty()){
            tilEmail.setError(getString(R.string.empty_email));
            valid = false;
        }else{
            if (Helper.isValidEmail(email)){
                tilEmail.setError("");
            }else{
                tilEmail.setError(getString(R.string.invalid_email));
                valid = false;
            }
        }
        String password = edPassword.getText().toString();
        if (password.length()<6){
            tilPassword.setError(getString(R.string.short_password));
            valid = false;
        }
        if (valid){
            loadingDialog.show();
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    loadingDialog.cancel();
                    if (task.isSuccessful()){
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user.isEmailVerified()){
                            Intent intent = new Intent(LoginActivity.this,MapActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            Helper.showEmailVerificationDialog(LoginActivity.this,email,user, Helper.LOGIN_MODE);
                        }
                    }else{
                        tilPassword.setError(getString(R.string.login_failed));
                    }
                }
            });
        }
    }
}