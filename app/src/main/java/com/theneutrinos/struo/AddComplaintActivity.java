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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
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

public class AddComplaintActivity extends AppCompatActivity {

    private EditText complaintSubjectET;
    private EditText complaintDescriptionET;
    private ImageButton addComplaintImageIB;
    private final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Complaints");
    private DatabaseReference newComplaint;
    private final DatabaseReference notRootRef = FirebaseDatabase.getInstance().getReference().child("User Details");
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private File output;
    private Uri imageUri;
    private Button submitComplaintB;
    private Spinner categoryTypeSpinner;
    private StorageReference storageRef;
    private String itemSelectedInSpinner;
    private static final int galleryRequestCode = 138;
    private static final int cameraRequestCode = 223;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_complaint);
        addComplaintImageIB = (ImageButton) findViewById(R.id.complaint_imageButton);
        complaintDescriptionET = (EditText) findViewById(R.id.complaint_description);
        complaintSubjectET = (EditText) findViewById(R.id.complaint_subject);
        addItemsToCategorySpinner();
        categoryTypeSpinner = (Spinner) findViewById(R.id.category_type_spinner);
        categoryTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                itemSelectedInSpinner = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                itemSelectedInSpinner = "Academics";
            }
        });
        submitComplaintB = (Button) findViewById(R.id.submit_complaint_button);
        storageRef = FirebaseStorage.getInstance().getReference();
        addComplaintImageIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(AddComplaintActivity.this)
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
        submitComplaintB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new startComplainingAT().execute();
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
        output = new File(dir, "ComplaintImage.jpeg");
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));
        startActivityForResult(cameraIntent, cameraRequestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == galleryRequestCode && resultCode == RESULT_OK)
        {
            imageUri = data.getData();
            addComplaintImageIB.setImageURI(imageUri);
        }
        if(requestCode == cameraRequestCode && resultCode == RESULT_OK)
        {
            imageUri = Uri.fromFile(output);
            addComplaintImageIB.setImageURI(imageUri);
        }
    }

    public class startComplainingAT extends AsyncTask<Void, Void, Void>
    {
        final String complaintSubjectS = complaintSubjectET.getText().toString().trim();
        final String complaintDescriptionS = complaintDescriptionET.getText().toString().trim();

        @Override
        protected void onPreExecute() {
            if(!TextUtils.isEmpty(complaintDescriptionS) && !TextUtils.isEmpty(complaintSubjectS) && imageUri == null)
            {
                Toast.makeText(AddComplaintActivity.this, "Uploading your complaint", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AddComplaintActivity.this, ComplaintsActivity.class));
            }
            else if(!TextUtils.isEmpty(complaintDescriptionS) && !TextUtils.isEmpty(complaintSubjectS) && imageUri !=null)
            {
                Toast.makeText(AddComplaintActivity.this, "Uploading your complaint", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AddComplaintActivity.this, ComplaintsActivity.class));
            }
            else
            {
                Toast.makeText(AddComplaintActivity.this, "Fields are empty", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            if(!TextUtils.isEmpty(complaintDescriptionS) && !TextUtils.isEmpty(complaintSubjectS) && imageUri != null)
            {
                StorageReference filepath = storageRef.child("ComplaintFeedImage").child(imageUri.getLastPathSegment());
                filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUri = taskSnapshot.getDownloadUrl();
                        newComplaint = rootRef.push();
                        String userUid = user.getUid();
                        DatabaseReference uidRef = notRootRef.child(userUid);
                        newComplaint.child("Subject").setValue(complaintSubjectS);
                        newComplaint.child("Description").setValue(complaintDescriptionS);
                        newComplaint.child("Image").setValue(downloadUri.toString());
                        newComplaint.child("Category").setValue(itemSelectedInSpinner);
                        newComplaint.child("Userid").setValue(userUid);
                        uidRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                newComplaint.child("Name").setValue(dataSnapshot.child("Name").getValue().toString());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                        newComplaint.child("Time").setValue(currentDateTimeString);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddComplaintActivity.this, "Your complaint has been posted", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AddComplaintActivity.this, "Failed to upload your complaint\nPlease check your internet connection", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
            else if(!TextUtils.isEmpty(complaintDescriptionS) && !TextUtils.isEmpty(complaintSubjectS) && imageUri == null)
            {
                newComplaint = rootRef.push();
                String userUid = user.getUid();
                DatabaseReference uidRef = notRootRef.child(userUid);
                newComplaint.child("Subject").setValue(complaintSubjectS);
                newComplaint.child("Description").setValue(complaintDescriptionS);
                newComplaint.child("Category").setValue(itemSelectedInSpinner);
                newComplaint.child("Userid").setValue(userUid);
                uidRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        newComplaint.child("Name").setValue(dataSnapshot.child("Name").getValue().toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                newComplaint.child("Time").setValue(currentDateTimeString);
            }
            return null;
        }
    }

    public void addItemsToCategorySpinner()
    {
        categoryTypeSpinner = (Spinner) findViewById(R.id.category_type_spinner);
        ArrayAdapter<CharSequence> categoryTypeSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.category, android.R.layout.simple_spinner_item);
        categoryTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryTypeSpinner.setAdapter(categoryTypeSpinnerAdapter);
    }
}
