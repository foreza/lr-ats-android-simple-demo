package com.liveramp.ats.sampleapp;

import android.app.Activity;
import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import org.prebid.mobile.ExternalUserId;
import org.prebid.mobile.Host;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.TargetingParams;
import org.prebid.mobile.api.data.AdUnitFormat;
import org.prebid.mobile.api.data.InitializationStatus;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.rendering.InterstitialAdUnit;
import org.prebid.mobile.api.rendering.listeners.InterstitialAdUnitListener;

import java.util.EnumSet;


public class AdManagerPrebid {

    private static final String TAG = "AdManager-Prebid";

    // Example ad unit from Prebid
    private static final String CONFIG_ID = "prebid-ita-video-interstitial-320-480";

    private InterstitialAdUnit adUnit;


    // Set the email in the signal provider as soon as you have it
    public void setRampIDEnvelopeForPrebid(String envelope) {
        TargetingParams.storeExternalUserId(new ExternalUserId("liveramp.com", envelope, null, null));
        Log.d(TAG, "Set identity in prebid for liveramp successfully! (Targeting Params)");
    }


    public void initPrebid(Activity ctx) {

        PrebidMobile.setShareGeoLocation(true);
        PrebidMobile.setPrebidServerAccountId("0689a263-318d-448b-a3d4-b02e8a709d9d");
        PrebidMobile.setCustomStatusEndpoint("https://prebid-server-test-j.prebid.org/status");
        PrebidMobile.setPrebidServerHost(
                Host.createCustomHost(
                        "https://prebid-server-test-j.prebid.org/openrtb2/auction"
                )
        );

        PrebidMobile.initializeSdk(ctx, status -> {
            if (status == InitializationStatus.SUCCEEDED) {
                Log.d(TAG, "Prebid SDK initialized successfully!");
                TargetingParams.addUserKeyword("healthy");
                TargetingParams.addUserKeyword("targeted");
                TargetingParams.setUserId("123456");
                TargetingParams.setGender(TargetingParams.GENDER.MALE);
            } else {
                Log.e(TAG, "Prebid SDK initialization error: " + status.getDescription());
            }
        });
    }


    public void makePrebidAdRequest(Activity ctx) {

        adUnit = new InterstitialAdUnit(ctx,CONFIG_ID, EnumSet.of(AdUnitFormat.VIDEO));
        adUnit.setInterstitialAdUnitListener(new InterstitialAdUnitListener() {
            @Override
            public void onAdLoaded(InterstitialAdUnit interstitialAdUnit) {
                Log.d(TAG, "onAdLoaded");
                Toast.makeText(ctx, "onAdLoaded", Toast.LENGTH_SHORT).show();
                interstitialAdUnit.show();
            }

            @Override
            public void onAdFailed(InterstitialAdUnit interstitialAdUnit, AdException exception) {
                Log.e(TAG, "onAdFailed" + exception.getMessage());
                // Handle the error.
                adUnit = null;
                Toast.makeText(ctx, "onAdFailed", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onAdDisplayed(InterstitialAdUnit interstitialAdUnit) {
                Log.d(TAG, "onAdDisplayed");
            }

            @Override
            public void onAdClicked(InterstitialAdUnit interstitialAdUnit) {}

            @Override
            public void onAdClosed(InterstitialAdUnit interstitialAdUnit) {}
        });

        adUnit.loadAd();


    }


    private static AdManagerPrebid instance = null;

    private AdManagerPrebid() {}

    public static AdManagerPrebid getInstance()
    {
        // To ensure only one instance is created
        if (instance == null) {
            instance = new AdManagerPrebid();
        }
        return instance;
    }

}
