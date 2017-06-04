package com.theneutrinos.struo;

import android.app.TimePickerDialog;
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
import android.widget.TimePicker;
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

public class ActuallyAddEventActivity extends AppCompatActivity {

    private String eventDateS;
    private String endTimeS;
    private String startTimeS;
    private ImageButton selectEventImageIB;
    private EditText startTimeET;
    private EditText endTimeET;
    private EditText eventTitleET;
    private EditText eventDescriptionET;
    private Button createEventB;
    private TimePickerDialog timePickerDialog;
    private TimePickerDialog timePickerDialog1;
    private File output;
    private Uri imageUri;
    private java.util.Calendar calendar;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference rootRef;
    private final DatabaseReference notRootRef = FirebaseDatabase.getInstance().getReference().child("User Details");
    private DatabaseReference newEvent;
    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private int eventStartHour;
    private int eventStartMinute;
    private int eventEndHour;
    private int eventEndMinute;
    private static final int galleryRequestCode = 1;
    private static final int cameraRequestCode = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actually_add_event);
        Intent intent = getIntent();
        eventDateS = intent.getStringExtra("date");
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        selectEventImageIB = (ImageButton) findViewById(R.id.event_image_button);
        startTimeET = (EditText) findViewById(R.id.start_time);
        endTimeET = (EditText) findViewById(R.id.end_time);
        eventTitleET = (EditText) findViewById(R.id.event_title);
        eventDescriptionET = (EditText) findViewById(R.id.event_description);
        createEventB = (Button) findViewById(R.id.create_event_button);
        rootRef = FirebaseDatabase.getInstance().getReference().child("Events").child(eventDateS);
        calendar = java.util.Calendar.getInstance();
        eventEndHour = eventStartHour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        eventEndMinute = eventStartMinute = calendar.get(java.util.Calendar.MINUTE);
        startTimeET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog = new TimePickerDialog(ActuallyAddEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String hourOfDayS = Integer.toString(hourOfDay);
                        String minuteS = Integer.toString(minute);
                        if (hourOfDayS.length() == 1)
                        {
                            hourOfDayS = "0" + hourOfDayS;
                        }
                        if (minuteS.length() == 1)
                        {
                            minuteS = "0" + minuteS;
                        }
                        startTimeS = hourOfDayS + ":" + minuteS;
                        startTimeET.setText(startTimeS);
                    }
                }, eventStartHour, eventStartMinute, true);
                timePickerDialog.setTitle("Select start time");
                timePickerDialog.show();
            }
        });
        endTimeET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog1 = new TimePickerDialog(ActuallyAddEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String hourOfDayS = Integer.toString(hourOfDay);
                        String minuteS = Integer.toString(minute);
                        if (hourOfDayS.length() == 1)
                        {
                            hourOfDayS = "0" + hourOfDayS;
                        }
                        if (minuteS.length() == 1)
                        {
                            minuteS = "0" + minuteS;
                        }
                        endTimeS = hourOfDayS + ":" + minuteS;
                        endTimeET.setText(endTimeS);
                    }
                }, eventEndHour, eventEndMinute, true);
                timePickerDialog1.setTitle("Select end time");
                timePickerDialog1.show();
            }
        });
        selectEventImageIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               new AlertDialog.Builder(ActuallyAddEventActivity.this)
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
        createEventB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new startCreatingEventAT().execute();
            }
        });
    }

    public void openIntentGallery()
    {
        startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), galleryRequestCode);
    }

    public void openIntentCamera()
    {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        output = new File(dir, "EventImage.jpeg");
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, output);
        startActivityForResult(cameraIntent, cameraRequestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == galleryRequestCode && resultCode == RESULT_OK)
        {
            imageUri = data.getData();
            selectEventImageIB.setImageURI(imageUri);
        }
        if (requestCode == cameraRequestCode && resultCode == RESULT_OK)
        {
            imageUri = Uri.fromFile(output);
            selectEventImageIB.setImageURI(imageUri);
        }
    }

    class startCreatingEventAT extends AsyncTask<Void, Void, Void>
    {
        final String eventTitleS = eventTitleET.getText().toString().trim();
        final String eventDescS = eventDescriptionET.getText().toString().trim();
        final String eventStartTimeS = startTimeET.getText().toString().trim();
        final String eventEndTimeS = endTimeET.getText().toString().trim();

        @Override
        protected void onPreExecute() {
            if(!TextUtils.isEmpty(eventTitleS) && !TextUtils.isEmpty(eventDescS) && !TextUtils.isEmpty(eventStartTimeS)  && !TextUtils.isEmpty(eventEndTimeS) && imageUri == null)
            {
                Toast.makeText(ActuallyAddEventActivity.this, "Creating Event", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ActuallyAddEventActivity.this, AddEventActivity.class).putExtra("date", eventDateS));
            }
            else if (!TextUtils.isEmpty(eventTitleS) && !TextUtils.isEmpty(eventEndTimeS) && !TextUtils.isEmpty(eventStartTimeS) && !TextUtils.isEmpty(eventDescS) && imageUri != null)
            {
                Toast.makeText(ActuallyAddEventActivity.this, "Creating Event", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ActuallyAddEventActivity.this, AddEventActivity.class).putExtra("date", eventDateS));
            }
            else
            {
                Toast.makeText(ActuallyAddEventActivity.this, "Fields are empty", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (!TextUtils.isEmpty(eventTitleS) && !TextUtils.isEmpty(eventEndTimeS) && !TextUtils.isEmpty(eventStartTimeS) && !TextUtils.isEmpty(eventDescS) && imageUri != null) {
                StorageReference filepath = storageRef.child("EventImages").child(eventDateS).child(imageUri.getLastPathSegment());
                filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUri = taskSnapshot.getDownloadUrl();
                        newEvent = rootRef.push();
                        String userUid = user.getUid();
                        DatabaseReference uidRef = notRootRef.child(userUid);
                        newEvent.child("Title").setValue(eventTitleS);
                        newEvent.child("Description").setValue(eventDescS);
                        newEvent.child("Date").setValue(eventDateS);
                        newEvent.child("StartTime").setValue(eventStartTimeS);
                        newEvent.child("EndTime").setValue(eventEndTimeS);
                        newEvent.child("Image").setValue(downloadUri.toString());
                        newEvent.child("Userid").setValue(userUid);
                        uidRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                newEvent.child("Mobileno").setValue(dataSnapshot.child("Mobileno").getValue().toString());
                                newEvent.child("Name").setValue(dataSnapshot.child("Name").getValue().toString());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ActuallyAddEventActivity.this, "New event has been created", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ActuallyAddEventActivity.this, "Event could not be created\nPlease check your internet connection", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }
            else if (!TextUtils.isEmpty(eventTitleS) && !TextUtils.isEmpty(eventEndTimeS) && !TextUtils.isEmpty(eventStartTimeS) && !TextUtils.isEmpty(eventDescS) && imageUri == null)
            {
                newEvent = rootRef.push();
                String userUid = user.getUid();
                Toast.makeText(ActuallyAddEventActivity.this, userUid, Toast.LENGTH_SHORT).show();
                DatabaseReference uidRef = notRootRef.child(userUid);
                newEvent.child("Title").setValue(eventTitleS);
                newEvent.child("Description").setValue(eventDescS);
                newEvent.child("Date").setValue(eventDateS);
                newEvent.child("StartTime").setValue(eventStartTimeS);
                newEvent.child("EndTime").setValue(eventEndTimeS);
                newEvent.child("Userid").setValue(userUid);
                uidRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        newEvent.child("Mobileno").setValue(dataSnapshot.child("Mobileno").getValue().toString());
                        newEvent.child("Name").setValue(dataSnapshot.child("Name").getValue().toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ActuallyAddEventActivity.this, "New event was created", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ActuallyAddEventActivity.this, AddEventActivity.class).putExtra("date", eventDateS));
    }
}
