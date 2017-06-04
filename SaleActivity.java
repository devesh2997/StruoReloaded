package com.theneutrinos.struo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class SaleActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private RecyclerView adFeed;
    private DatabaseReference rootARef;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private LinearLayoutManager linearLayoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale);
        linearLayoutManager = new LinearLayoutManager(SaleActivity.this);
        fab = (FloatingActionButton) findViewById(R.id.fab_sell);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SaleActivity.this, AdvertiseActivity.class));
            }
        });
        adFeed = (RecyclerView) findViewById(R.id.ad_feed);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        adFeed.setHasFixedSize(true);
        adFeed.setLayoutManager(linearLayoutManager);
        rootARef = FirebaseDatabase.getInstance().getReference().child("Ads");
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<AdFeed, AdFeedViewHolder> adFeedAdFeedViewHolderFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<AdFeed, AdFeedViewHolder>(AdFeed.class, R.layout.sale_feed_row, AdFeedViewHolder.class, rootARef) {
            @Override
            protected void populateViewHolder(AdFeedViewHolder viewHolder, final AdFeed model, int position) {
                final String adKey = getRef(position).getKey();
                viewHolder.setTime(model.getTime());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setName(model.getName());
                viewHolder.setTitle(model.getItem());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.callSellerIB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:"+model.getMobileno()));
                        //todo: HANDLE POTENTIAL SECURITY EXCEPTION
                        try {
                            startActivity(intent);
                        }
                        catch (SecurityException se)
                        {
                            Toast.makeText(SaleActivity.this, "Call permission is currently disabled", Toast.LENGTH_SHORT).show();
                            se.printStackTrace();
                        }
                    }
                });
                viewHolder.view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if ((auth.getCurrentUser().getUid()).equals(model.getUserid()))
                        {
                            new AlertDialog.Builder(SaleActivity.this)
                                    .setTitle("Delete Ad?")
                                    .setMessage("Do you really want to delete your advertisement?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            DatabaseReference deleteRef = FirebaseDatabase.getInstance().getReference().child("Ads").child(adKey);
                                            deleteRef.removeValue();
                                            StorageReference deleteStorageRef = FirebaseStorage.getInstance().getReference().child("AdImages").child(model.getImage());
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
        adFeed.setAdapter(adFeedAdFeedViewHolderFirebaseRecyclerAdapter);
    }

    public static class AdFeedViewHolder extends  RecyclerView.ViewHolder
    {
        View view;
        ImageButton callSellerIB;
        public AdFeedViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            callSellerIB = (ImageButton) view.findViewById(R.id.call_seller);
        }

        public void setTitle(String title)
        {
            TextView titleTV = (TextView) view.findViewById(R.id.sale_item_card_view);
            titleTV.setText(title);
        }

        public void setTime(String time)
        {
            TextView timeTV = (TextView) view.findViewById(R.id.sale_date_card_view);
            timeTV.setText(time);
        }

        public void setDesc(String desc)
        {
            TextView descTV = (TextView) view.findViewById(R.id.sale_desc_card_view);
            descTV.setText(desc);
        }

        public void setImage (Context context, String uri)
        {
            ImageView adImageIV= (ImageView) view.findViewById(R.id.sale_select_image_card_view);
            Picasso.with(context).load(uri).into(adImageIV);
        }

        public void setName(String name)
        {
            TextView nameTV = (TextView) view.findViewById(R.id.sale_name_card_view);
            nameTV.setText(name);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(SaleActivity.this, MainActivity.class));
    }
}
