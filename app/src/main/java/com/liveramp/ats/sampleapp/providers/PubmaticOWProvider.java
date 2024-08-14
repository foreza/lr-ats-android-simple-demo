package com.liveramp.ats.sampleapp.providers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.pubmatic.sdk.common.OpenWrapSDK;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.common.models.POBApplicationInfo;
import com.pubmatic.sdk.openwrap.interstitial.POBInterstitial;

import java.net.MalformedURLException;
import java.net.URL;


// Source: https://github.com/PubMatic/android-openwrap-sdk-samples/blob/master/OpenWrap/Basic/OpenWrap/JavaSample/app/src/main/java/com/pubmatic/openwrap/app/InterstitialActivity.java
public class PubmaticOWProvider {

    private static final String OPENWRAP_AD_UNIT_ID = "OpenWrapInterstitialAdUnit";
    private static final String PUB_ID = "156276";
    private static final int PROFILE_ID = 1165;

    private POBInterstitial interstitial;

    public void setup(Context context){

        POBApplicationInfo appInfo = new POBApplicationInfo();
        try {
            appInfo.setStoreURL(new URL("https://play.google.com/store/apps/details?id=com.example.android&hl=en"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // This app information is a global configuration & you
        // Need not set this for every ad request(of any ad type)
        OpenWrapSDK.setApplicationInfo(appInfo);

        // Create  interstitial instance by passing activity context and
        interstitial = new POBInterstitial(context, PUB_ID,
                PROFILE_ID,
                OPENWRAP_AD_UNIT_ID);

        interstitial.setListener(new PubmaticOWProvider.POBInterstitialListener());


    }


    public void makeRequest(){
        interstitial.loadAd();
    }


    class POBInterstitialListener extends POBInterstitial.POBInterstitialListener {
        private final String TAG = "POBInterstitialListener";
        // Callback method notifies that an ad has been received successfully.
        @Override
        public void onAdReceived(@NonNull POBInterstitial ad) {
            Log.d(TAG, "onAdReceived: " +  ad.isReady());

            if (null != interstitial) {
                // Call show on interstitial
                 interstitial.show();
            }
        }

        // Callback method notifies an error encountered while loading an ad.
        @Override
        public void onAdFailedToLoad(@NonNull POBInterstitial ad, @NonNull POBError error) {
            Log.e(TAG, "onAdFailedToLoad : Ad failed to load with error -" + error.toString());
        }

        // Callback method notifies an error encountered while showing an ad.
        @Override
        public void onAdFailedToShow(@NonNull POBInterstitial ad, @NonNull POBError error) {
            Log.e(TAG, "onAdFailedToShow : Ad failed to show with error -" + error.toString());
        }

        // Callback method notifies that the interstitial ad will be presented as a modal on top of the current view controller
        @Override
        public void onAdOpened(@NonNull POBInterstitial ad) {
            Log.d(TAG, "onAdOpened");
        }

        // Callback method notifies that the interstitial ad has been animated off the screen.
        @Override
        public void onAdClosed(@NonNull POBInterstitial ad) {
            Log.d(TAG, "onAdClosed");
        }

        // Callback method notifies ad click
        @Override
        public void onAdClicked(@NonNull POBInterstitial ad) {
            Log.d(TAG, "onAdClicked");
        }

        // Callback method Notifies whenever current app goes in the background due to user click
        @Override
        public void onAppLeaving(@NonNull POBInterstitial ad) {
            // Implement your custom logic
            Log.d(TAG, "Interstitial : App Leaving");
        }

        public void onAdImpression(@NonNull POBInterstitial ad) {
            Log.d(TAG, "onAdImpression");
        }
    }


    // Singleton
    private static PubmaticOWProvider instance = null;

    private PubmaticOWProvider() {}

    public static PubmaticOWProvider getInstance()
    {
        // To ensure only one instance is created
        if (instance == null) {
            instance = new PubmaticOWProvider();
        }
        return instance;
    }



}
