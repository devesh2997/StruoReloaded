package com.theneutrinos.struo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

    private EditText emailFieldET;
    private EditText passwordFieldET;
    private Button loginButtonB;
    private Button mainSignUpB;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        progressDialog = new ProgressDialog(this);
        emailFieldET = (EditText) findViewById(R.id.email_field);
        passwordFieldET = (EditText) findViewById(R.id.password);
        loginButtonB = (Button) findViewById(R.id.login_button);
        mainSignUpB = (Button) findViewById(R.id.sign_up);
        auth = FirebaseAuth.getInstance();
        loginButtonB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSignIn();
                progressDialog.setMessage("Signing In...");
                progressDialog.show();
            }
        });
        mainSignUpB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToSignUpPage = new Intent(SignInActivity.this, RegistrationActivity.class);
                finish();
                startActivity(goToSignUpPage);
            }
        });
    }

    private void startSignIn()
    {
        String email = emailFieldET.getText().toString().trim();
        String password = passwordFieldET.getText().toString();
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Fields are empty", Toast.LENGTH_SHORT).show();
        }
        else
        {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(SignInActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                    else
                    {
                        Toast.makeText(SignInActivity.this, "Signed In", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        finish();

                        startActivity(new Intent(SignInActivity.this, MainActivity.class));
                    }
                    //progressDialog.dismiss();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        System.exit(0);
    }
}
