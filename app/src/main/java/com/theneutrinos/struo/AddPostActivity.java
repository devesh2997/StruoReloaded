package com.theneutrinos.struo;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

public class AddPostActivity extends AppCompatActivity {

    private ImageButton selectImageIB;
    private EditText postTitleET;
    private EditText postDescET;
    private Button submitPostB;
    private Uri imageUri = null;
    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("News");
    private final DatabaseReference notRootRef = FirebaseDatabase.getInstance().getReference().child("User Details");
    private DatabaseReference newPost;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private File output;
    private static final int galleryRequestCode = 1;
    public static final int cameraRequestCode = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        selectImageIB = (ImageButton) findViewById(R.id.imageButton);
        postDescET = (EditText) findViewById(R.id.post_description);
        postTitleET = (EditText) findViewById(R.id.post_title);
        submitPostB = (Button) findViewById(R.id.post_button);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        selectImageIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(AddPostActivity.this)
                        .setTitle("Upload Image")
                        .setMessage("Where do you want to upload the image from?")
                        .setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                openIntentGallery();
                            }
                        }).setNeutralButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openIntentCamera();
                    }
                }).show();
                }

        });
        submitPostB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new startPostingAT().execute();
            }
        });
    }
    public void openIntentGallery() {
        startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), galleryRequestCode);
    }

    public void openIntentCamera()
    {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        output = new File(dir, "PostImage.jpeg");
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));
        startActivityForResult(cameraIntent, cameraRequestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == galleryRequestCode && resultCode == RESULT_OK)
        {
            imageUri = data.getData();
            selectImageIB.setImageURI(imageUri);
        }
        if(requestCode == cameraRequestCode && resultCode == RESULT_OK)
        {
            imageUri = Uri.fromFile(output);
            selectImageIB.setImageURI(imageUri);
        }
    }

    class startPostingAT extends AsyncTask<Void, Void, Void>
    {
        final String postTitleS = postTitleET.getText().toString().trim();
        final String postDescS = postDescET.getText().toString().trim();

        @Override
        protected void onPreExecute() {
            if(!TextUtils.isEmpty(postDescS) && !TextUtils.isEmpty(postTitleS) && imageUri != null)
            {
                Toast.makeText(AddPostActivity.this, "Uploading your post", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AddPostActivity.this, MainActivity.class));
            }
            else
            {
                Toast.makeText(AddPostActivity.this, "Fields are empty", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            if(!TextUtils.isEmpty(postDescS) && !TextUtils.isEmpty(postTitleS) && imageUri != null)
            {
                StorageReference filepath = storageRef.child("NewsFeedImage").child(imageUri.getLastPathSegment());
                filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUri = taskSnapshot.getDownloadUrl();
                        newPost = rootRef.push();
                        String userUid = user.getUid();
                        DatabaseReference uidRef = notRootRef.child(userUid);
                        newPost.child("Title").setValue(postTitleS);
                        newPost.child("Desc").setValue(postDescS);
                        newPost.child("Image").setValue(downloadUri.toString());
                        newPost.child("Userid").setValue(userUid);
                        uidRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                newPost.child("Name").setValue(dataSnapshot.child("Name").getValue().toString());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                        newPost.child("Time").setValue(currentDateTimeString);
                        //TODO: add a method to show a toast that the post was uploaded
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddPostActivity.this, "Your post was uploaded", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //TODO: add a method to show a toast that the post wasn't uploaded
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddPostActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
            return null;
        }
    }
}
