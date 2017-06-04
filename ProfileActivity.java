package com.theneutrinos.struo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ProfileActivity extends AppCompatActivity {

    private Uri imageUri;
    private File output;
    private Button updateButton;
    private ImageButton profileImageIB;
    private EditText nameET;
    private EditText mobilenoET;
    private EditText emailET;
    private DatabaseReference userDetailsRef;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private Animation completelyFadeOut, completelyFadeOut2, completelyFadeOut3;
    private Animation completelyFadeIn, completelyFadeIn2, completelyFadeIn3, completelyFadeIn4;
    private ProgressDialog progressDialog;
    private static final int galleryRequestCode = 1;
    private static final int cameraRequestCode = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        profileImageIB = (ImageButton) findViewById(R.id.profile_image_button);
        nameET = (EditText) findViewById(R.id.profile_name_view);
        emailET = (EditText) findViewById(R.id.profile_email_view);
        mobilenoET = (EditText) findViewById(R.id.profile_mobileno_view);
        updateButton = (Button) findViewById(R.id.update_button);
        completelyFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.completely_fade_out);
        completelyFadeOut2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.completely_fade_out);
        completelyFadeOut3 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.completely_fade_out);
        completelyFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.completely_fade_out);
        completelyFadeIn2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.completely_fade_out);
        completelyFadeIn3 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.completely_fade_out);
        completelyFadeIn4 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.completely_fade_in);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        progressDialog = new ProgressDialog(this);
        userDetailsRef = FirebaseDatabase.getInstance().getReference().child("User Details").child(user.getUid());
        userDetailsRef.keepSynced(true);
        userDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nameET.setText(dataSnapshot.child("Name").getValue().toString());
                emailET.setText(user.getEmail());
                mobilenoET.setText(dataSnapshot.child("Mobileno").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        profileImageIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle("Upload Image")
                        .setMessage("Where do you want to upload the image from?")
                        .setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), galleryRequestCode);
                            }
                        })
                        .setNeutralButton("Camera", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                output = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "ProfilePhoto.jpeg");
                                startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output)), cameraRequestCode);
                            }
                        })
                        .show();
            }
        });
        nameET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                {
                    nameET.setBackgroundResource(R.drawable.button_background_2);
                    nameET.startAnimation(completelyFadeOut);
                    nameET.setTextColor(Color.BLACK);
                }
                else if (!hasFocus)
                {
                    nameET.setBackgroundResource(R.drawable.input_background_2);
                    nameET.startAnimation(completelyFadeIn);
                    nameET.setTextColor(Color.WHITE);
                }
            }
        });
        emailET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                {
                    emailET.setBackgroundResource(R.drawable.button_background_2);
                    emailET.startAnimation(completelyFadeOut2);
                    emailET.setTextColor(Color.BLACK);
                }
                else if (!hasFocus)
                {
                    emailET.setBackgroundResource(R.drawable.input_background_2);
                    emailET.startAnimation(completelyFadeIn2);
                    emailET.setTextColor(Color.WHITE);
                }
            }
        });
        mobilenoET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                {
                    mobilenoET.setBackgroundResource(R.drawable.button_background_2);
                    mobilenoET.startAnimation(completelyFadeOut3);
                    mobilenoET.setTextColor(Color.BLACK);
                }
                else if (!hasFocus)
                {
                    mobilenoET.setBackgroundResource(R.drawable.input_background_2);
                    mobilenoET.startAnimation(completelyFadeIn3);
                    mobilenoET.setTextColor(Color.WHITE);
                }
            }
        });
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateButton.startAnimation(completelyFadeIn4);
                progressDialog.setMessage("Updating info");
                progressDialog.show();
                startUpdating();
            }
        });
    }

    public void startUpdating()
    {
        final String nameS = nameET.getText().toString().trim();
        final String emailS = emailET.getText().toString().trim();
        final String mobilenoS = mobilenoET.getText().toString().trim();
        if(!TextUtils.isEmpty(nameS) && !TextUtils.isEmpty(emailS) && !TextUtils.isEmpty(mobilenoS))
        {
            userDetailsRef.child("Name").setValue(nameS);
            userDetailsRef.child("Mobileno").setValue(mobilenoS);
        }
        else
        {
            Toast.makeText(this, "Fields are empty", Toast.LENGTH_SHORT).show();
        }
        progressDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == galleryRequestCode && resultCode == RESULT_OK)
        {
            imageUri = data.getData();
            profileImageIB.setImageURI(imageUri);
        }
        else if (requestCode == cameraRequestCode && resultCode == RESULT_OK)
        {
            imageUri = Uri.fromFile(output);
            profileImageIB.setImageURI(imageUri);
        }
    }
}
