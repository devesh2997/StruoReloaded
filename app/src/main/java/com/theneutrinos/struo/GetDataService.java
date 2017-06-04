package com.theneutrinos.struo;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GetDataService extends Service {

    private DatabaseReference rootRef;
    private DatabaseReference notRootRef;
    private String notifName;
    private String notifTitle;
    NotificationCompat.Builder builder;
    private static final int uniqueID = 34795;
    private final IBinder iBinder = new MyLocalBinder();
    public GetDataService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        notRootRef = FirebaseDatabase.getInstance().getReference().child("User Details");
        rootRef = FirebaseDatabase.getInstance().getReference().child("News");
        return iBinder;
    }

    public class MyLocalBinder extends Binder
    {
        GetDataService getService()
        {
            return GetDataService.this;
        }
    }

    public class getDataForNotification extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            rootRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    notifName = dataSnapshot.child("Name").getValue().toString();
                    notifTitle = dataSnapshot.child("Title").getValue().toString();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return null;
        }
    }
}
