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

public class RegistrationActivity extends AppCompatActivity {

    private Button signUpB;
    private EditText emailET;
    private EditText passwordET;
    private EditText confirmPasswordET;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        signUpB =(Button) findViewById(R.id.sign_up_button);
        emailET = (EditText) findViewById(R.id.register_email_field);
        passwordET = (EditText) findViewById(R.id.register_password);
        confirmPasswordET = (EditText) findViewById(R.id.enter_password_again);
        signUpB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerNewUser();
            }
        });
    }

    private void registerNewUser()
    {
        final String emailS = emailET.getText().toString().trim();
        String passwordS = passwordET.getText().toString();
        String confirmPasswordS = confirmPasswordET.getText().toString();
        if (passwordS.length() < 6)
        {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(emailS)||TextUtils.isEmpty(passwordS))
        {
            Toast.makeText(this, "Fields are empty", Toast.LENGTH_LONG).show();
            return;
        }
        else
        {
            if(TextUtils.equals(passwordS, confirmPasswordS))
            {
                progressDialog.setMessage("Signing Up");
                progressDialog.show();
                firebaseAuth.createUserWithEmailAndPassword(emailS, passwordS).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            //user is successfully registered and logged in
                            //start profile activity
                            progressDialog.dismiss();
                            Intent signedIn = new Intent(RegistrationActivity.this, GetDetailsAfterRegistration.class);
                            //signedIn.putExtra("EmailPE", emailET.getText().toString().trim());
                            finish();
                            startActivity(signedIn);
                        }
                        else
                        {
                            Toast.makeText(RegistrationActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else
            {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                passwordET.setText("");
                confirmPasswordET.setText("");
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent goingBack = new Intent(this, MainActivity.class);
        finish();
        startActivity(goingBack);
    }
}
