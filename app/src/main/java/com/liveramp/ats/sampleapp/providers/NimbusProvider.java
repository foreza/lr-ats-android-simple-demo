package com.liveramp.ats.sampleapp.providers;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.adsbynimbus.Nimbus;
import com.adsbynimbus.NimbusAdManager;
import com.adsbynimbus.NimbusError;
import com.adsbynimbus.render.AdController;
import com.adsbynimbus.request.NimbusRequest;
import com.adsbynimbus.request.NimbusResponse;
import com.adsbynimbus.request.OkHttpNimbusClient;
import com.adsbynimbus.request.RequestManager;


import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

// Utility class to show/test Nimbus Ad Requests
// Source: https://github.com/adsbynimbus/nimbus-android-sample/blob/main/app/src/main/kotlin/rendering/NimbusAdManager.kt
// Test Init: https://github.com/adsbynimbus/nimbus-android-sample/blob/main/gradle.properties
// Integration docs https://docs.adsbynimbus.com/docs/sdk/android/integration
public class NimbusProvider {

    private final String TAG = "NimbusAdManager";
    private final String PUBLISHER_KEY = "dev-sdk";
    private final String API_KEY = "d352cac1-cae2-4774-97ba-4e15c6276be0";
    private final String REQUEST_URL = "https://"+ PUBLISHER_KEY +".adsbynimbus.com/rta/test";

    NimbusAdManager manager = null;
    Activity activityRef = null;

    public void setup(Context context, Activity activity){

        Nimbus.initialize(context, PUBLISHER_KEY, API_KEY);
        Nimbus.setTestMode(true);
        Nimbus.usPrivacyString = "1YNY";
        Nimbus.COPPA = false;

        RequestManager.setRequestUrl(REQUEST_URL);
        RequestManager.setClient(new OkHttpNimbusClient(new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                    @Override
                    public void log(String message) {
                        Log.d(TAG, "Nimbus Request: "+ message);
                    }
                }).setLevel(HttpLoggingInterceptor.Level.BODY))));

        manager = new NimbusAdManager();
        activityRef = activity;

        Log.d(TAG, "Nimbus Initialized? "+  Nimbus.INSTANCE.isInitialized());
    }


    public void makeRequest(){

        // TODO: This doesn't actually render; figure out why it doesn't
        manager.showBlockingAd(NimbusRequest.forInterstitialAd("position"), activityRef, new NimbusAdManager.Listener() {
            @Override
            public void onError(@NonNull NimbusError error) {
                Log.e(TAG, "onError: " +  error.errorType);
            }

            @Override
            public void onAdResponse(@NonNull NimbusResponse nimbusResponse) {
                Log.d(TAG, "onAdResponse: " + nimbusResponse.toString());
            }

            @Override
            public void onAdRendered(@NonNull AdController controller) {
                Log.d(TAG, "onAdRendered: ");
            }
        });

    }



    // Singleton
    private static NimbusProvider instance = null;

    private NimbusProvider() {}

    public static NimbusProvider getInstance()
    {
        // To ensure only one instance is created
        if (instance == null) {
            instance = new NimbusProvider();
        }
        return instance;
    }



}
