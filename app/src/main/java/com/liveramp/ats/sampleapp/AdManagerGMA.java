package com.liveramp.ats.sampleapp;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
import com.liveramp.ats.*;
import com.liveramp.ats.model.*;

import java.util.Map;
import java.util.Objects;

public class AdManagerGMA {

    private static final String TAG = "AdManager-GMA";
    private RewardedInterstitialAd rewardedInterstitialAd;


    // Example ad unit from Google
    private static final String AD_UNIT_ID = "/21775744923/example/rewarded_interstitial";

    // Configure GMA
    // Note: Calling LRAtsMediationAdapter does not actually kick off anything.
    // This prepares LRAtsMediationAdapter to be called by Google
    // Once the GMA SDK initializes; LRAtsMediationAdapter will then try to fetch an envelope
    public void configureLiveRampGMASignalProvider(String configId){
        LRAtsConfiguration config = new LRAtsConfiguration(configId, false, false, null);
        LRAtsMediationAdapter.setHasConsentForNoLegislation(true);
        LRAtsMediationAdapter.configure(config);
    }


    // Set the email in the signal provider as soon as you have it
    public void setEmailForLiveRampGMASignalProvider(String email) {
        LRAtsMediationAdapter.setIdentifier(new LREmailIdentifier(email));
    }


    public void initGMA(Activity ctx){

        // Log the Mobile Ads SDK version.
        Log.d(TAG, "Google Mobile Ads SDK Version: " + MobileAds.getVersion());


        MobileAds.initialize(
                ctx,
                new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {
                        Map<String, AdapterStatus> tMap = initializationStatus.getAdapterStatusMap();

                        for (Map.Entry<String, AdapterStatus> entry : tMap.entrySet()) {
                            Log.d(TAG, "Adapter: " + entry.getValue());
                            // You should see LiveRamp as one of these adapters.
                            if (Objects.equals(entry.getKey(), "com.liveramp.ats.LRAtsMediationAdapter")) {
                                Log.d(TAG, "LiveRamp Adapter found!");
                            }
                        }

                    }
                });
    }

    public void makeGMAAdRequest(Activity ctx) {

        if (rewardedInterstitialAd != null) {
            Log.e(TAG, "Ad is loading or exists already");
            return;
        }

        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();

        RewardedInterstitialAd.load(
                ctx,
                AD_UNIT_ID,
                adRequest,
                new RewardedInterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(RewardedInterstitialAd ad) {
                        Log.d(TAG, "onAdLoaded");

                        rewardedInterstitialAd = ad;

                        showGMAAd(ctx);
                        Toast.makeText(ctx, "onAdLoaded", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        Log.d(TAG, "onAdFailedToLoad: " + loadAdError.getMessage());

                        // Handle the error.
                        rewardedInterstitialAd = null;
                        Toast.makeText(ctx, "onAdFailedToLoad", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void showGMAAd(Activity ctx){

        if (rewardedInterstitialAd == null) {
            Log.d(TAG, "The rewarded interstitial ad wasn't ready yet.");
            return;
        }

        rewardedInterstitialAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    /** Called when ad showed the full screen content. */
                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d(TAG, "onAdShowedFullScreenContent");

                        Toast.makeText(ctx, "onAdShowedFullScreenContent", Toast.LENGTH_SHORT)
                                .show();
                    }

                    /** Called when the ad failed to show full screen content. */
                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        Log.d(TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());

                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        rewardedInterstitialAd = null;

                        Toast.makeText(
                                        ctx, "onAdFailedToShowFullScreenContent", Toast.LENGTH_SHORT)
                                .show();
                    }

                    /** Called when full screen content is dismissed. */
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        rewardedInterstitialAd = null;
                        Log.d(TAG, "onAdDismissedFullScreenContent");
                        Toast.makeText(ctx, "onAdDismissedFullScreenContent", Toast.LENGTH_SHORT)
                                .show();
                    }
                });

        Activity activityContext = ctx;

        rewardedInterstitialAd.show(
                activityContext,
                new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        // Handle the reward.
                        Log.d(TAG, "The user earned a reward.");
                    }
                });

    }



    private static AdManagerGMA instance = null;

    private AdManagerGMA() {}

    public static AdManagerGMA getInstance()
    {
        // To ensure only one instance is created
        if (instance == null) {
            instance = new AdManagerGMA();
        }
        return instance;
    }

}
