package com.android.touristguide;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

public class SignUpActivity extends AppCompatActivity {
    private EditText edEmail;
    private EditText edPassword;
    private EditText edConfirmPassword;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private FirebaseAuth mAuth;
    private EditText edUsername;
    private TextInputLayout tilUsername;
    private FirebaseFunctions mFunctions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edEmail = (EditText)findViewById(R.id.ed_email_signup);
        edPassword = (EditText) findViewById(R.id.ed_password_signup);
        edConfirmPassword = (EditText) findViewById(R.id.ed_confirm_password_signup);
        tilEmail = (TextInputLayout) findViewById(R.id.til_email);
        tilPassword = (TextInputLayout) findViewById(R.id.til_password);
        tilConfirmPassword = (TextInputLayout) findViewById(R.id.til_confirm_password);
        tilUsername = (TextInputLayout) findViewById(R.id.til_username);
        edUsername = (EditText) findViewById(R.id.ed_username);
        mAuth = FirebaseAuth.getInstance();
        mFunctions = Helper.initFirebaseFunctions();

        findViewById(R.id.btn_signup).setOnClickListener(view -> signUp());

        edUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tilUsername.setError("");
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

        edConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tilConfirmPassword.setError("");
            }
        });
    }

    private void signUp(){
        final String email = edEmail.getText().toString().trim();
        final String password = edPassword.getText().toString().trim();
        String confirmPassword = edConfirmPassword.getText().toString().trim();
        final String username = edUsername.getText().toString().trim();

        boolean valid = true;

        if (username.isEmpty()){
            tilUsername.setError(getString(R.string.empty_username));
            valid = false;
        }

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

        if (password.length()<6){
            tilPassword.setError(getString(R.string.short_password));
            valid = false;
        }

        if (!confirmPassword.equals(password)){
            tilConfirmPassword.setError(getString(R.string.confirmation_not_match));
            valid = false;
        }

        if (valid){
            final AlertDialog loadingDialog = Helper.createLoadingDialog(this);
            loadingDialog.show();
            mAuth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                            if (task.getResult().getSignInMethods().isEmpty()){
                                mAuth.createUserWithEmailAndPassword(email,password)
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()){
                                                    mAuth.getCurrentUser().sendEmailVerification();
                                                    Helper.signUp(username,email,"",mFunctions)
                                                            .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                                                                    loadingDialog.cancel();
                                                                    if (task.isSuccessful()){
                                                                        Helper.showEmailVerificationDialog(SignUpActivity.this, email, mAuth.getCurrentUser(), Helper.SIGN_UP_MODE);
                                                                    }else{
                                                                        Log.d("SignUpActivity",task.getException().toString());
                                                                        Toast.makeText(SignUpActivity.this,task.getException().toString(), Toast.LENGTH_LONG).show();
                                                                    }
                                                                }
                                                            });
                                                }else{
                                                    loadingDialog.cancel();
                                                    Toast.makeText(SignUpActivity.this,getString(R.string.sign_up_failed),Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                            }else{
                                tilEmail.setError(getString(R.string.existing_email));
                                edEmail.requestFocus();
                                loadingDialog.cancel();
                            }
                        }
                    });

        }
    }

}
