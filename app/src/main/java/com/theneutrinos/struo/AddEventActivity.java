package com.theneutrinos.struo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class AddEventActivity extends AppCompatActivity {

    private long noOfExistingEvents;
    private FloatingActionButton floatingActionButton;
    private RecyclerView addEventActivityRecyclerView;
    private String eventDateS;
    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private DatabaseReference databaseInterested;
    private Boolean processInterested;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        Intent intent = getIntent();
        eventDateS = intent.getStringExtra("date");
        databaseInterested = FirebaseDatabase.getInstance().getReference().child("Interested");
        databaseInterested.keepSynced(true);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.add_event_fab);
        addEventActivityRecyclerView = (RecyclerView) findViewById(R.id.add_event_recycler_view);
        rootRef = FirebaseDatabase.getInstance().getReference().child("Events").child(eventDateS);
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                noOfExistingEvents = dataSnapshot.getChildrenCount();
                if (noOfExistingEvents == 0)
                {
                    Toast.makeText(AddEventActivity.this, "There are no events on this day", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        rootRef.keepSynced(true);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noOfExistingEvents >=1 ) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddEventActivity.this);
                    builder.setTitle("An event already exists")
                            .setMessage("Do you really want to create an event on this day?")
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            startActivity(new Intent(AddEventActivity.this, ActuallyAddEventActivity.class).putExtra("date", eventDateS));
                        }
                    }).create().show();
                }
                else if (noOfExistingEvents == 0)
                {
                    finish();
                    startActivity(new Intent(AddEventActivity.this, ActuallyAddEventActivity.class).putExtra("date", eventDateS));
                }
            }
        });
        addEventActivityRecyclerView = (RecyclerView) findViewById(R.id.add_event_recycler_view);
        addEventActivityRecyclerView.setHasFixedSize(true);
        addEventActivityRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        auth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<EventsFeed, EventsFeedViewHolder> eventsFeedEventsFeedViewHolderFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<EventsFeed, EventsFeedViewHolder>(EventsFeed.class, R.layout.event_row, EventsFeedViewHolder.class, rootRef) {
            @Override
            protected void populateViewHolder(EventsFeedViewHolder viewHolder, final EventsFeed model, int position) {
                final String eventKey = getRef(position).getKey();
                viewHolder.setTitle(model.getTitle());
                viewHolder.setName(model.getName());
                viewHolder.setDesc(model.getDescription());
                viewHolder.setEndTime(model.getEndTime());
                viewHolder.setStartTime(model.getStartTime());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setInterestedImageButton(eventKey);
                viewHolder.setInterestedTextView(eventKey);
                //TODO: this function does not work as desired
                viewHolder.interestedImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        processInterested = true;
                        databaseInterested.keepSynced(true);
                        databaseInterested.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (processInterested)
                                {
                                    databaseInterested.keepSynced(true);
                                    if (dataSnapshot.child(eventKey).hasChild(auth.getCurrentUser().getUid()))
                                    {
                                        databaseInterested.child(eventKey).child(auth.getCurrentUser().getUid()).removeValue();
                                        processInterested = false;
                                    }
                                    else
                                    {
                                        final String userUid = auth.getCurrentUser().getUid();
                                        DatabaseReference detailsRef = FirebaseDatabase.getInstance().getReference().child("User Details").child(userUid);
                                        detailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                databaseInterested.child(eventKey).child(userUid).child("Name").setValue(dataSnapshot.child("Name").getValue().toString());
                                                databaseInterested.child(eventKey).child(userUid).child("Mobileno").setValue(dataSnapshot.child("Mobileno").getValue().toString());
                                                databaseInterested.child(eventKey).child(userUid).child("Registrationno").setValue(dataSnapshot.child("Registrationno").getValue().toString());
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                        processInterested = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
                viewHolder.view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if ((auth.getCurrentUser().getUid()).equals(model.getUserid())) {
                            new AlertDialog.Builder(AddEventActivity.this)
                                    .setTitle("Delete Event?")
                                    .setMessage("Do you really want to delete this event?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            DatabaseReference deleteRef = FirebaseDatabase.getInstance().getReference().child("Events").child(eventKey);
                                            deleteRef.removeValue();
                                            StorageReference delStorageRef = FirebaseStorage.getInstance().getReference().child("EventImages").child(model.getImage());
                                            delStorageRef.delete();
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    }).show();
                        }
                        return true;
                    }
                });
            }
        };
        addEventActivityRecyclerView.setAdapter(eventsFeedEventsFeedViewHolderFirebaseRecyclerAdapter);
    }

    public static class EventsFeedViewHolder extends RecyclerView.ViewHolder
    {
        View view;
        ImageButton interestedImageButton;
        DatabaseReference databaseInterested;
        DatabaseReference notGettingNameForRef;
        FirebaseAuth auth;
        public EventsFeedViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            interestedImageButton = (ImageButton) view.findViewById(R.id.interested_image_button);
            auth = FirebaseAuth.getInstance();
            databaseInterested = FirebaseDatabase.getInstance().getReference().child("Interested");
            databaseInterested.keepSynced(true);
        }

        private void setInterestedImageButton(final String eventKey)
        {
            databaseInterested.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(eventKey).hasChild(auth.getCurrentUser().getUid()))
                    {
                        interestedImageButton.setImageResource(R.mipmap.ic_star_black_24dp);
                    }
                    else
                    {
                        interestedImageButton.setImageResource(R.mipmap.ic_star_border_black_24dp);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        private void setInterestedTextView(final String eventKey)
        {
            notGettingNameForRef = databaseInterested.child(eventKey);
            notGettingNameForRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Long noOfPeopleInterestedL = dataSnapshot.getChildrenCount();
                    TextView interestedTextView = (TextView) view.findViewById(R.id.no_of_interested);
                    interestedTextView.setText(noOfPeopleInterestedL.toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setTitle(String title)
        {
            TextView textView = (TextView) view.findViewById(R.id.event_title_card_view);
            textView.setText(title);
        }

        private void setDesc(String desc)
        {
            TextView textView = (TextView) view.findViewById(R.id.event_desc_card_view);
            textView.setText(desc);
        }

        private void setStartTime(String startTime)
        {
            TextView textView = (TextView) view.findViewById(R.id.event_start_time_card_view);
            textView.setText(startTime);
        }

        private void setEndTime (String endTime)
        {
            TextView textView = (TextView) view.findViewById(R.id.event_end_time_card_view);
            textView.setText(endTime);
        }

        public void setName(String name)
        {
            TextView textView = (TextView) view.findViewById(R.id.event_name_card_view);
            textView.setText(name);
        }

        private void setImage(Context imageContext, String uri)
        {
            ImageView imageView = (ImageView) view.findViewById(R.id.event_image_card_view);
            Picasso.with(imageContext).load(uri).into(imageView);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AddEventActivity.this, EventsActivity.class));
    }
}
