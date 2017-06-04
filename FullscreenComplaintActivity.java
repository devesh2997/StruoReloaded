package com.theneutrinos.struo;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.text.DateFormat;

public class FullscreenComplaintActivity extends AppCompatActivity {

    private TextView fsNameTV;
    private TextView fsDescTV;
    private TextView fsSubjectTV;
    private ImageView fsImageIV;
    private TextView fsTimeTV;
    private String fscomplaintsKeyS;
    private EditText newCommentET;
    private ImageButton sendNewCommentButton;
    private ImageButton fsUpvoteIB;
    private RecyclerView commentsFeedRV;
    private DatabaseReference databaseUpvote;//points to upvotes
    private DatabaseReference notGettingNameForRef;//points to upvotes -> complaintKey
    private DatabaseReference commentsRootRef;//points to complaints comments -> complaintKey
    private DatabaseReference newCommentRef;//points to commentsRootRef.push();
    private DatabaseReference notRootRef;//points to user Details
    private FirebaseAuth auth;
    private FirebaseUser user;
    private Boolean processUpvote = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_complaint);
        fsDescTV = (TextView) findViewById(R.id.fullscreen_complaints_desc_card_view);
        fsTimeTV = (TextView) findViewById(R.id.fullscreen_complaints_date_card_view);
        fsNameTV = (TextView) findViewById(R.id.fullscreen_complaints_name_card_view);
        fsSubjectTV = (TextView) findViewById(R.id.fullscreen_complaints_title_card_view);
        fsImageIV = (ImageView) findViewById(R.id.fullscreen_complaints_image_card_view);
        fsUpvoteIB = (ImageButton) findViewById(R.id.fullscreen_complaints_upvote_button);
        commentsFeedRV = (RecyclerView) findViewById(R.id.comments_recycler_view);
        newCommentET = (EditText) findViewById(R.id.add_comment_edit_text_view);
        sendNewCommentButton = (ImageButton) findViewById(R.id.send_comment_button);
        commentsFeedRV.setHasFixedSize(true);
        commentsFeedRV.setLayoutManager(new LinearLayoutManager(this));
        databaseUpvote = FirebaseDatabase.getInstance().getReference().child("Upvotes");
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        Intent activityThatCalled = getIntent();
        String fsNameS = activityThatCalled.getExtras().getString("NameFCA");
        String fsDescS = activityThatCalled.getExtras().getString("DescriptionFCA");
        String fsSubjectS = activityThatCalled.getExtras().getString("SubjectFCA");
        String fsTimeS = activityThatCalled.getExtras().getString("TimeFCA");
        String fsImageS;
        Uri imageUri;
        try
        {
            fsImageS = activityThatCalled.getExtras().getString("ImageFCA");
            imageUri = Uri.parse(fsImageS);
            Picasso.with(getApplicationContext()).load(imageUri).into(fsImageIV);
        }
        catch (Exception e)
        {
            Log.d("neutrinos.struo", "imageUriString is null");
        }
        fscomplaintsKeyS =  activityThatCalled.getExtras().getString("ComplaintKeyFCA");
        commentsRootRef = FirebaseDatabase.getInstance().getReference().child("Complaints Comments").child(fscomplaintsKeyS);
        commentsRootRef.keepSynced(true);
        notRootRef = FirebaseDatabase.getInstance().getReference().child("User Details");
        notRootRef.keepSynced(true);
        fsNameTV.setText(fsNameS);
        fsDescTV.setText(fsDescS);
        fsSubjectTV.setText(fsSubjectS);
        fsTimeTV.setText(fsTimeS);
        databaseUpvote.keepSynced(true);
        newCommentET.requestFocus();
        fsUpvoteIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processUpvote = true;
                databaseUpvote.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(processUpvote)
                        {
                            if (dataSnapshot.child(fscomplaintsKeyS).hasChild(auth.getCurrentUser().getUid()))
                            {
                                databaseUpvote.child(fscomplaintsKeyS).child(auth.getCurrentUser().getUid()).removeValue();
                                processUpvote = false;
                            }
                            else
                            {
                                databaseUpvote.child(fscomplaintsKeyS).child(auth.getCurrentUser().getUid()).setValue("R");
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
        ////////WORKING/////////////////////////////////////////////////////////////////////////////
        databaseUpvote.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(fscomplaintsKeyS).hasChild(auth.getCurrentUser().getUid()))
                {
                    fsUpvoteIB.setImageResource(R.mipmap.ic_thumb_up_red_24dp);
                }
                else
                {
                    fsUpvoteIB.setImageResource(R.mipmap.ic_thumb_up_black_24dp);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        notGettingNameForRef = databaseUpvote.child(fscomplaintsKeyS);
        notGettingNameForRef.keepSynced(true);
        notGettingNameForRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long noOfUpvotes = dataSnapshot.getChildrenCount();
                TextView fsNoOfUpvotesTV = (TextView) findViewById(R.id.fullscreen_complaints_number_of_upvotes);
                fsNoOfUpvotesTV.setText(noOfUpvotes.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        ////////////////////////////////////////////////////////////////////////////////////////////

        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        sendNewCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentsRootRef.keepSynced(true);
                startCommenting();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        commentsRootRef.keepSynced(true);
        FirebaseRecyclerAdapter<CommentsFeed, CommentsFeedViewHolder> commentsFeedCommentsFeedViewHolderFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<CommentsFeed, CommentsFeedViewHolder>(CommentsFeed.class, R.layout.comment_row, CommentsFeedViewHolder.class, commentsRootRef) {
            @Override
            protected void populateViewHolder(CommentsFeedViewHolder viewHolder, final CommentsFeed model, int position) {
                final String commentKey = getRef(position).getKey();
                viewHolder.setName(model.getName());
                viewHolder.setTime(model.getTime());
                viewHolder.setComment(model.getComment());
                viewHolder.view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if ((auth.getCurrentUser().getUid()).equals(model.getUserid()))
                        {
                            new AlertDialog.Builder(FullscreenComplaintActivity.this)
                                    .setTitle("Delete comment?")
                                    .setMessage("Do you really want to delete your comment?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            DatabaseReference deleteRef = FirebaseDatabase.getInstance().getReference().child("Complaints Comments").child(fscomplaintsKeyS).child(commentKey);
                                            deleteRef.removeValue();
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
        commentsFeedRV.setAdapter(commentsFeedCommentsFeedViewHolderFirebaseRecyclerAdapter);
    }

    public static class CommentsFeedViewHolder extends RecyclerView.ViewHolder
    {
        View view;
        public CommentsFeedViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setName(String CommenterName)
        {
            TextView commentersNameTV = (TextView) view.findViewById(R.id.comment_name);
            commentersNameTV.setText(CommenterName);
        }

        public void setTime(String CommentTime)
        {
            TextView commentTimeTV = (TextView) view.findViewById(R.id.comment_time);
            commentTimeTV.setText(CommentTime);
        }

        private void setComment(String CommentComment)
        {
            TextView commentCommentTV = (TextView) view.findViewById(R.id.comment_comment);
            commentCommentTV.setText(CommentComment);
        }
    }

    public void startCommenting()
    {
        final String newCommentS = newCommentET.getText().toString().trim();
        if(!TextUtils.isEmpty(newCommentS))
        {
            newCommentRef = commentsRootRef.push();
            newCommentRef.keepSynced(true);
            commentsRootRef.keepSynced(true);
            newCommentRef.child("Comment").setValue(newCommentS);
            newCommentET.setText("");
            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            newCommentRef.child("Time").setValue(currentDateTimeString);
            newCommentRef.child("Userid").setValue(user.getUid());
            DatabaseReference uidRef = notRootRef.child(user.getUid());
            uidRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    newCommentRef.child("Name").setValue(dataSnapshot.child("Name").getValue().toString().trim());
                    newCommentRef.keepSynced(true);
                    commentsRootRef.keepSynced(true);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
