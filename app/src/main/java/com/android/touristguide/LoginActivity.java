package com.android.touristguide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
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
    private FirebaseFunctions firebaseFunctions;

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
        firebaseFunctions = Helper.initFirebaseFunctions();

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

    private void requestLocationPermission(){
        Log.d(TAG,"Logined!");
        if (Build.VERSION.SDK_INT>=23){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},2);
            }else{
                if (Build.VERSION.SDK_INT>=29 && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},3);
                }else{
                    startMainActivity();
                }
            }
        }else{
            startMainActivity();
        }
    }

    private void showPermissionDeniedDialog(){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.location_permission_denied));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2){
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                if (Build.VERSION.SDK_INT>=29 &&ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},3);
                }else{
                    startMainActivity();
                }
            }else{
                showPermissionDeniedDialog();
            }
        }else{
            if (requestCode==3){
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    startMainActivity();
                }else{
                    showPermissionDeniedDialog();
                }
            }
        }
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
        final AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String photo = account.getPhotoUrl().toString().replace("s96-c","s720-c");
                            Helper.signUp(account.getDisplayName(), account.getEmail(),photo,firebaseFunctions)
                                   .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                                       @Override
                                       public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                                           loadingDialog.cancel();
                                           if (task.isSuccessful()){
                                               requestLocationPermission();
                                           }else{
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
                            requestLocationPermission();
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
    private void startMainActivity(){
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}