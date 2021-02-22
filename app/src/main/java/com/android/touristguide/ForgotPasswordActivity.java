package com.android.touristguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;

public class ForgotPasswordActivity extends AppCompatActivity {
    private Button btnReset;
    private TextInputLayout tilEmail;
    private EditText edEmail;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        btnReset = (Button) findViewById(R.id.btn_reset);
        tilEmail = (TextInputLayout) findViewById(R.id.til_email);
        edEmail = (EditText) findViewById(R.id.ed_email);
        mAuth = FirebaseAuth.getInstance();

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

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();
            }
        });
    }

    private void reset(){
        final String email = edEmail.getText().toString().trim();
        final AlertDialog loadingDialog = Helper.createLoadingDialog(this);
        if (Helper.isValidEmail(email)){
            loadingDialog.show();
            mAuth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                            List<String> signInMethods = task.getResult().getSignInMethods();
                            if (signInMethods.isEmpty()){
                                loadingDialog.cancel();
                                tilEmail.setError(getString(R.string.email_not_exist));
                            }else{
                                if (!signInMethods.get(0).equals("password")){
                                    loadingDialog.cancel();
                                    tilEmail.setError(getString(R.string.email_not_exist));
                                }else{
                                    mAuth.sendPasswordResetEmail(email)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        loadingDialog.cancel();
                                                        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(new ContextThemeWrapper(ForgotPasswordActivity.this, R.style.MaterialAlertDialog));
                                                        dialog.setMessage(R.string.reset_password_email_sent)
                                                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                                        finish();
                                                                    }
                                                                }).show();
                                                    }
                                                }
                                            });
                                    Log.d("ForgotPassword",signInMethods.get(0));
                                }
                            }
                        }
                    });
        }else{
            tilEmail.setError(getString(R.string.invalid_email));
        }
    }
}