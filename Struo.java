package com.theneutrinos.struo;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Aman Deep Singh on 24-01-2017.
 */

public class Struo extends Application
{
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
