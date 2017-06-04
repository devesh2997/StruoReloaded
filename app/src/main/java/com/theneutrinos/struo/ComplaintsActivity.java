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

public class ComplaintsActivity extends AppCompatActivity {

    private DatabaseReference rootCRef;
    public DatabaseReference databaseUpvote;
    private RecyclerView complaintsFeed;
    private FloatingActionButton fab;
    private FirebaseAuth auth;
    private LinearLayoutManager linearLayoutManager;
    private Boolean processUpvote = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaints);
        linearLayoutManager = new LinearLayoutManager(ComplaintsActivity.this);
        rootCRef = FirebaseDatabase.getInstance().getReference().child("Complaints");
        complaintsFeed = (RecyclerView) findViewById(R.id.complaints_feed);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        complaintsFeed.setHasFixedSize(true);
        complaintsFeed.setLayoutManager(linearLayoutManager);
        auth = FirebaseAuth.getInstance();
        databaseUpvote = FirebaseDatabase.getInstance().getReference().child("Upvotes");
        fab = (FloatingActionButton) findViewById(R.id.fab_complaints);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ComplaintsActivity.this, AddComplaintActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<ComplaintsFeed, ComplaintsFeedViewHolder> complaintsFeedComplaintsFeedViewHolderFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ComplaintsFeed, ComplaintsFeedViewHolder>(ComplaintsFeed.class, R.layout.complaints_feed_row, ComplaintsFeedViewHolder.class, rootCRef) {
            @Override
            protected void populateViewHolder(ComplaintsFeedViewHolder viewHolder, final ComplaintsFeed model, int position) {
                final String complaintKey = getRef(position).getKey();
                viewHolder.setName(model.getName());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setSubject(model.getSubject());
                viewHolder.setTime(model.getTime());
                viewHolder.setUpvoteButton(complaintKey);
                viewHolder.setUpvotes(complaintKey);
                viewHolder.upvoteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        processUpvote = true;
                        databaseUpvote.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(processUpvote)
                                {
                                    if(dataSnapshot.child(complaintKey).hasChild(auth.getCurrentUser().getUid()))
                                    {
                                        databaseUpvote.child(complaintKey).child(auth.getCurrentUser().getUid()).removeValue();
                                        processUpvote = false;
                                    }
                                    else
                                    {
                                        databaseUpvote.child(complaintKey).child(auth.getCurrentUser().getUid()).setValue("R");
                                        processUpvote = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ComplaintsActivity.this, FullscreenComplaintActivity.class);
                        intent.putExtra("NameFCA", model.getName());
                        intent.putExtra("DescriptionFCA", model.getDescription());
                        intent.putExtra("ImageFCA", model.getImage());
                        intent.putExtra("SubjectFCA", model.getSubject());
                        intent.putExtra("TimeFCA", model.getTime());
                        intent.putExtra("ComplaintKeyFCA", complaintKey);
                        startActivity(intent);
                    }
                });
                viewHolder.view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if ((auth.getCurrentUser().getUid()).equals(model.getUserid()))
                        {
                            new AlertDialog.Builder(ComplaintsActivity.this)
                                    .setTitle("Delete complaint?")
                                    .setMessage("Do you really want to delete your complaint?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            DatabaseReference deleteRef = FirebaseDatabase.getInstance().getReference().child("Complaints").child(complaintKey);
                                            deleteRef.removeValue();
                                            StorageReference deleteStorageRef = FirebaseStorage.getInstance().getReference().child("ComplaintFeedImage").child(model.getImage());
                                            deleteStorageRef.delete();
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
        complaintsFeed.setAdapter(complaintsFeedComplaintsFeedViewHolderFirebaseRecyclerAdapter);
    }

    public static class ComplaintsFeedViewHolder extends RecyclerView.ViewHolder{

        View view;
        ImageButton upvoteButton;
        DatabaseReference databaseUpvote;
        DatabaseReference notGettingNameForRef;
        FirebaseAuth auth;
        public ComplaintsFeedViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            upvoteButton = (ImageButton) view.findViewById(R.id.complaints_upvote_button);
            databaseUpvote = FirebaseDatabase.getInstance().getReference().child("Upvotes");
            auth = FirebaseAuth.getInstance();
            databaseUpvote.keepSynced(true);
        }

        public void setUpvoteButton(final String complaintKey)
        {
            databaseUpvote.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(complaintKey).hasChild(auth.getCurrentUser().getUid()))
                    {
                        upvoteButton.setImageResource(R.mipmap.ic_thumb_up_red_24dp);
                    }
                    else
                    {
                        upvoteButton.setImageResource(R.mipmap.ic_thumb_up_black_24dp);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setName(String Name)
        {
            TextView nameCTV = (TextView) view.findViewById(R.id.complaints_name_card_view);
            nameCTV.setText(Name);
        }

        public void setDescription(String Description)
        {
            TextView descriptionCTV = (TextView) view.findViewById(R.id.complaints_desc_card_view);
            descriptionCTV.setText(Description);
        }

        private void setImage(Context context,  String imageUri)
        {
            ImageView imageCIV = (ImageView) view.findViewById(R.id.complaints_image_card_view);
            Picasso.with(context).load(imageUri).into(imageCIV);
        }

        private void setSubject(String subject)
        {
            TextView subjectCTV = (TextView) view.findViewById(R.id.complaints_title_card_view);
            subjectCTV.setText(subject);
        }

        public void setTime(String Time)
        {
            TextView timeCTV = (TextView) view.findViewById(R.id.complaints_date_card_view);
            timeCTV.setText(Time);
        }

        private void setUpvotes(final String complaintKey)
        {
            notGettingNameForRef = databaseUpvote.child(complaintKey);
            notGettingNameForRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Long noOfUpvotes = dataSnapshot.getChildrenCount();
                    TextView noOfUpvotesTV = (TextView) view.findViewById(R.id.complaints_number_of_upvotes);
                    noOfUpvotesTV.setText(noOfUpvotes.toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(ComplaintsActivity.this, MainActivity.class));
    }
}
