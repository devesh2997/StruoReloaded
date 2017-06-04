package com.theneutrinos.struo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    FloatingActionButton complain, event, cart, fab;
    boolean isOpen = false;
    Animation FabOpen, FabClose, FabClockwise, FabAnticlockwise, FabCloseCW, FadeIn, FadeOut;
    private FirebaseUser user;
    public ProgressDialog progressDialog;
    public boolean isPosting = false;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private RecyclerView newsFeed;
    private DatabaseReference rootRef;//points to News
    private DatabaseReference databaseLike;//points to likes
    private DatabaseReference notRootRef;//points to User Details -> user.getUid();
    private Boolean processLike = false;
    private LinearLayoutManager linearLayoutManager;
    private View coordinatorLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressDialog = new ProgressDialog(this);
        /*Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);*/
        //startActivity(new Intent(this, AddPostActivity.class));
        auth = FirebaseAuth.getInstance();
        fab = (FloatingActionButton) findViewById(R.id.fab);
        complain = (FloatingActionButton) findViewById(R.id.complain);
        cart = (FloatingActionButton) findViewById(R.id.cart);
        event = (FloatingActionButton) findViewById(R.id.event);
        complain.setAlpha(0.0f);
        cart.setAlpha(0.0f);
        event.setAlpha(0.0f);
        FabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        FabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        FabClockwise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_clockwise);
        FabAnticlockwise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_anticlockwise);
        FabCloseCW = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close_rotate_clockwise);
        FadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        FadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        coordinatorLayout = findViewById(R.id.include);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(MainActivity.this, AddPostActivity.class));
                //TODO Make a method to actually access AddPostActivity
                complain.setAlpha(1.0f);
                event.setAlpha(1.0f);
                cart.setAlpha(1.0f);
                if(isOpen)
                {
                    complain.startAnimation(FabCloseCW);
                    event.startAnimation(FabCloseCW);
                    cart.startAnimation(FabCloseCW);
                    fab.startAnimation(FabAnticlockwise);
                    coordinatorLayout.startAnimation(FadeOut);
                    complain.setClickable(false);
                    event.setClickable(false);
                    cart.setClickable(false);
                    isOpen = false;
                }
                else
                {
                    complain.startAnimation(FabOpen);
                    //complain.startAnimation(FabAnticlockwise);
                    event.startAnimation(FabOpen);
                    event.startAnimation(FabAnticlockwise);
                    cart.startAnimation(FabOpen);
                    //cart.startAnimation(FabAnticlockwise);
                    coordinatorLayout.startAnimation(FadeIn);
                    fab.startAnimation(FabClockwise);
                    complain.setClickable(true);
                    event.setClickable(true);
                    cart.setClickable(true);
                    isOpen = true;
                }
            }
        });
        complain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(MainActivity.this, ComplaintsActivity.class));
            }
        });
        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(MainActivity.this, SaleActivity.class));
            }
        });
        event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(MainActivity.this, EventsActivity.class));
            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        databaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        rootRef = FirebaseDatabase.getInstance().getReference().child("News");
        rootRef.keepSynced(true);
        databaseLike.keepSynced(true);
        newsFeed = (RecyclerView) findViewById(R.id.news_feed);
        newsFeed.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        newsFeed.setLayoutManager(linearLayoutManager);
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null)
                {
                    notRootRef = FirebaseDatabase.getInstance().getReference().child("User Details").child(user.getUid());
                    notRootRef.keepSynced(true);
                    notRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (!dataSnapshot.hasChild("Name") || !dataSnapshot.hasChild("Mobileno") || !dataSnapshot.hasChild("Registrationno") || !dataSnapshot.hasChild("Branch"))
                            {
                                finish();
                                startActivity(new Intent(MainActivity.this, GetDetailsAfterRegistration.class));
                            }
                            else
                            {
                                View headerView = navigationView.getHeaderView(0);
                                TextView nav_username = (TextView) headerView.findViewById(R.id.navigation_drawer_email_id);
                                nav_username.setText(dataSnapshot.child("Name").getValue().toString());
                                headerView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                else
                {
                    finish();
                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
        FirebaseRecyclerAdapter<NewsFeed, NewsFeedViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<NewsFeed, NewsFeedViewHolder>(NewsFeed.class, R.layout.news_feed_row, NewsFeedViewHolder.class, rootRef) {
            @Override
            protected void populateViewHolder(NewsFeedViewHolder viewHolder, final NewsFeed model, int position) {
                final String post_key = getRef(position).getKey();
                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setTime(model.getTime());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setName(model.getName());
                viewHolder.setLikeButton(post_key);
                viewHolder.setLikes(post_key);
                viewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        processLike = true;
                            databaseLike.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(processLike) {
                                        if (dataSnapshot.child(post_key).hasChild(auth.getCurrentUser().getUid())) {
                                            databaseLike.child(post_key).child(auth.getCurrentUser().getUid()).removeValue();
                                            processLike = false;
                                        } else {
                                            databaseLike.child(post_key).child(auth.getCurrentUser().getUid()).setValue("RandomValue");
                                            processLike = false;
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
                        if((auth.getCurrentUser().getUid()).equals(model.getUserid()))
                        {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Delete post?")
                                    .setMessage("Do you really want to delete your post?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            DatabaseReference deleteRef = FirebaseDatabase.getInstance().getReference().child("News").child(post_key);
                                            deleteRef.removeValue();
                                            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("NewsFeedImage").child(model.getImage());
                                            storageReference.delete();
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
        newsFeed.setAdapter(firebaseRecyclerAdapter);
    }

    public static class NewsFeedViewHolder extends RecyclerView.ViewHolder
    {
        View view;
        ImageButton likeButton;
        DatabaseReference databaseLike;
        DatabaseReference notGettingNameForRef;
        FirebaseAuth auth;
        public NewsFeedViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            likeButton = (ImageButton) view.findViewById(R.id.like_button);
            databaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            auth = FirebaseAuth.getInstance();
            databaseLike.keepSynced(true);
        }

        public void setLikeButton(final String post_key)
        {
            databaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(post_key).hasChild(auth.getCurrentUser().getUid()))
                    {
                        likeButton.setImageResource(R.mipmap.ic_thumb_up_red_24dp);
                    }
                    else
                    {
                        likeButton.setImageResource(R.mipmap.ic_thumb_up_black_24dp);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setTitle(String Title)
        {
            TextView titleTV = (TextView) view.findViewById(R.id.title_card_view);
            titleTV.setText(Title);
        }

        public void setDesc(String Desc)
        {
            TextView descTV = (TextView) view.findViewById(R.id.desc_card_view);
            descTV.setText(Desc);
        }

        public void setTime(String Time)
        {
            TextView timeTV = (TextView) view.findViewById(R.id.date_card_view);
            timeTV.setText(Time);
        }

        public void setImage(final Context context, final String imageUri)
        {
            final ImageView postImageIV = (ImageView) view.findViewById(R.id.image_card_view);
            Picasso.with(context).load(imageUri).into(postImageIV);
            /*Picasso.with(context).load(imageUri).networkPolicy(NetworkPolicy.OFFLINE).into(postImageIV, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(context).load(imageUri).into(postImageIV);
                }
            });*/
        }

        public void setName(String Name)
        {
            TextView nameTV = (TextView) view.findViewById(R.id.name_card_view);
            nameTV.setText(Name);
        }

        public void setLikes(final String post_key)
        {
            notGettingNameForRef = databaseLike.child(post_key);
            notGettingNameForRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Long noOfLikes = dataSnapshot.getChildrenCount();
                    TextView noOfLikesTV = (TextView) view.findViewById(R.id.number_of_likes);
                    noOfLikesTV.setText(noOfLikes.toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    ///////////////////////////////////
    //                               //
    //  Housekeeping stuff incoming  //
    //                               //
    ///////////////////////////////////

    @Override
    protected void onStop() {
        super.onStop();
        if(authStateListener != null)
            auth.removeAuthStateListener(authStateListener);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (isOpen) {
            complain.startAnimation(FabCloseCW);
            event.startAnimation(FabCloseCW);
            cart.startAnimation(FabCloseCW);
            fab.startAnimation(FabAnticlockwise);
            coordinatorLayout.startAnimation(FadeOut);
            complain.setClickable(false);
            event.setClickable(false);
            cart.setClickable(false);
            isOpen = false;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you really want to logout?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    auth.signOut();
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
            return true;
        }

        if (id == R.id.action_exit)
        {
            //TODO add exit functionality
        }

        if(id == R.id.action_add_post)
        {
            startActivity(new Intent(MainActivity.this, AddPostActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
