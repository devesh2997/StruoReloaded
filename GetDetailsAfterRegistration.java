package com.theneutrinos.struo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GetDetailsAfterRegistration extends AppCompatActivity {

    private EditText nameET;
    private EditText mobileNoET;
    private EditText registrationNoET;
    private Spinner branchSP;
    private DatabaseReference rootRef;
    private Button enterB;
    private FirebaseAuth auth;
    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_details_after_registration);
        nameET = (EditText) findViewById(R.id.name);
        rootRef = FirebaseDatabase.getInstance().getReference();
        mobileNoET = (EditText) findViewById(R.id.phone_number);
        registrationNoET = (EditText) findViewById(R.id.registration_number);
        addItemsToBranchSpinner();
        addListenerToBranchSpinner();
        enterB = (Button) findViewById(R.id.enter_button);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        enterB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mobileNoET.getText().toString().length() != 10)
                {
                    Toast.makeText(GetDetailsAfterRegistration.this, "Invalid mobile number", Toast.LENGTH_SHORT).show();
                }
                else if (mobileNoET.getText().toString().length() == 10)
                {
                    DatabaseReference childRef = rootRef.child("User Details").child(user.getUid());
                    childRef.child("Name").setValue(nameET.getText().toString().trim());
                    childRef.child("Mobileno").setValue(mobileNoET.getText().toString().trim());
                    childRef.child("Registrationno").setValue(registrationNoET.getText().toString().toUpperCase().trim());
                    //childRef.child("Branch").setValue(branchSP.getText().toString().trim());
                    startActivity(new Intent(GetDetailsAfterRegistration.this, MainActivity.class));
                }
            }
        });
    }

    private void addListenerToBranchSpinner()
    {
        branchSP = (Spinner) findViewById(R.id.branch);
        branchSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String itemSelectedSpinner = parent.getItemAtPosition(position).toString();
                DatabaseReference childRef = rootRef.child("User Details").child(user.getUid());
                childRef.child("Branch").setValue(itemSelectedSpinner);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(GetDetailsAfterRegistration.this, "Enter your branch", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addItemsToBranchSpinner()
    {
        branchSP = (Spinner) findViewById(R.id.branch);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.branches, R.layout.custom_spinner_item);
        spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        branchSP.setAdapter(spinnerAdapter);
    }
}
