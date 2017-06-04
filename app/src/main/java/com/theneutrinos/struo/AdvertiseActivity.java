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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

public class AdvertiseActivity extends AppCompatActivity {

    private EditText sellItemET;
    private EditText sellItemDescriptionET;
    private Button postAdB;
    private ImageButton selectItemImageIB;
    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("Ads");
    private final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Ads");
    private final DatabaseReference notRootRef = FirebaseDatabase.getInstance().getReference().child("User Details");
    private DatabaseReference newAd;
    private File output = null;
    private Uri imageUri;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    final private static int cameraRequestCode = 1;
    final private static int galleryRequestCode = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertise);
        sellItemET = (EditText) findViewById(R.id.sell_item);
        sellItemDescriptionET = (EditText) findViewById(R.id.sell_item_description);
        postAdB = (Button) findViewById(R.id.sell_button);
        selectItemImageIB = (ImageButton) findViewById(R.id.sell_image_button);
        selectItemImageIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(AdvertiseActivity.this)
                        .setTitle("Upload Image")
                        .setMessage("Where do you want to upload the image from?")
                        .setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                openIntentGallery();
                            }
                        })
                        .setNeutralButton("Camera", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                openIntentCamera();
                            }
                        }).show();
            }
        });
        postAdB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new startAdvertisingAT().execute();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == galleryRequestCode && resultCode == RESULT_OK)
        {
            imageUri = data.getData();
            selectItemImageIB.setImageURI(imageUri);
        }
        if(requestCode == cameraRequestCode && resultCode == RESULT_OK)
        {
            imageUri=Uri.fromFile(output);
            Picasso.with(getApplicationContext()).load(imageUri).into(selectItemImageIB);
        }
    }

    public void openIntentGallery() {
        startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), galleryRequestCode);
    }

    public void openIntentCamera()
    {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        output = new File(dir, "AdImage.jpeg");
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));
        startActivityForResult(cameraIntent, cameraRequestCode);
    }

    public class startAdvertisingAT extends AsyncTask<Void, Void, Void>
    {
        final String sellItemS = sellItemET.getText().toString().trim();
        final String sellItemDescriptionS = sellItemDescriptionET.getText().toString().trim();

        @Override
        protected void onPreExecute() {
            if(imageUri == null)
            {
                Toast.makeText(AdvertiseActivity.this, "Select an image", Toast.LENGTH_SHORT).show();
            }
            else
            {
                if(!TextUtils.isEmpty(sellItemS) && !TextUtils.isEmpty(sellItemDescriptionS))
                {
                    Toast.makeText(AdvertiseActivity.this, "Uploading your advertisement", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AdvertiseActivity.this, SaleActivity.class));
                }
                else
                {
                    Toast.makeText(AdvertiseActivity.this, "Fields are empty", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            if(!TextUtils.isEmpty(sellItemDescriptionS) && !TextUtils.isEmpty(sellItemS) && imageUri != null)
            {
                StorageReference filepath = storageRef.child("AdImages").child(imageUri.getLastPathSegment());
                filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUri = taskSnapshot.getDownloadUrl();
                        newAd = rootRef.push();
                        String userUid = user.getUid();
                        DatabaseReference uidARef = notRootRef.child(userUid);
                        newAd.child("Item").setValue(sellItemS);
                        newAd.child("Desc").setValue(sellItemDescriptionS);
                        newAd.child("Image").setValue(downloadUri.toString());
                        newAd.child("Userid").setValue(userUid);
                        uidARef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                newAd.child("Name").setValue(dataSnapshot.child("Name").getValue().toString());
                                newAd.child("Mobileno").setValue(dataSnapshot.child("Mobileno").getValue().toString());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                        newAd.child("Time").setValue(currentDateTimeString);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AdvertiseActivity.this, "Your ad was posted", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AdvertiseActivity.this, "Failed to post your ad", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(AdvertiseActivity.this, SaleActivity.class));
    }
}
