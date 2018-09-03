package com.id.connect.diaspora.service;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;
import android.support.v4.content.ContextCompat;

import com.ale.rainbowsdk.RainbowSdk;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.id.connect.diaspora.MainActivity;
import com.id.connect.diaspora.R;
import io.fabric.sdk.android.Fabric;

public class MessengerService extends Application {
    private static MessengerService mInstance;

    @Override
    protected void attachBaseContext(Context base){
        super.attachBaseContext(base);
        //ultiDex.install(this);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        mInstance = this;
        RainbowSdk.instance().setNotificationBuilder(
                getApplicationContext(),
                MainActivity.class,
                R.drawable.ic_notifications_black_24dp,
                getString(R.string.app_name),
                getResources().getString(R.string.notif_connecting),
                ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)
        );

        if(!RainbowSdk.instance().isInitialized()){
            RainbowSdk.instance().initialize(null, null);
        }
    }

    public static synchronized MessengerService getInstance() {
        return mInstance;
    }

    public void startService(){
        Intent mainService = new Intent(getApplicationContext(), RainbowService.class);
        startService(mainService);
    }

    public void stopService() {
        Intent mainService = new Intent(getApplicationContext(), RainbowService.class);
        stopService(mainService);
    }
}