package com.liveramp.ats.sampleapp.providers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.prebid.mobile.DataObject;
import org.prebid.mobile.Host;
import org.prebid.mobile.InterstitialAdUnit;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.data.AdUnitFormat;
import org.prebid.mobile.api.data.BidInfo;
import org.prebid.mobile.api.data.InitializationStatus;
import org.prebid.mobile.api.original.OnFetchDemandResult;

import java.util.EnumSet;

// Utility class to show/test Prebid Ad Requests
// https://github.com/prebid/prebid-mobile-android/blob/master/Example/PrebidDemoKotlin/src/main/java/org/prebid/mobile/prebidkotlindemo/activities/ads/inapp/InAppDisplayInterstitialActivity.kt
public class PrebidProvider {

    private final String TAG = "PrebidProvider";

    private final String CONFIG_ID = "prebid-demo-display-interstitial-320-480";
    private final String ACCOUNT_ID = "0689a263-318d-448b-a3d4-b02e8a709d9d";
    private final String STATUS_ENDPOINT = "https://prebid-server-test-j.prebid.org/status";
    private final String HOST_ENDPOINT = "https://prebid-server-test-j.prebid.org/openrtb2/auction";
    private InterstitialAdUnit adUnit;


    // Prebid takes AppContext
    public void setup(Context appContext){

        PrebidMobile.setShareGeoLocation(true);
        PrebidMobile.setPrebidServerAccountId(ACCOUNT_ID);
        PrebidMobile.setCustomStatusEndpoint(STATUS_ENDPOINT);
        PrebidMobile.setPrebidServerHost(
                Host.createCustomHost(
                        HOST_ENDPOINT
                )
        );

        PrebidMobile.initializeSdk(appContext, status -> {
            if (status == InitializationStatus.SUCCEEDED) {
                Log.d(TAG, "SDK initialized successfully!");
            } else {
                Log.e(TAG, "SDK initialization error: " + status.getDescription());
            }
        });

        adUnit = new InterstitialAdUnit(CONFIG_ID, EnumSet.of(AdUnitFormat.BANNER));
    }


    public void makeRequest(){

        Log.d("My external IDs:" , PrebidMobile.getExternalUserIds().toString());

        adUnit.fetchDemand(new OnFetchDemandResult() {
            @Override
            public void onComplete(@NonNull BidInfo bidInfo) {
                Log.d("Received bid: ", bidInfo.getResultCode().toString());

            }
        });
    }



    // Singleton
    private static PrebidProvider instance = null;

    private PrebidProvider() {}

    public static PrebidProvider getInstance()
    {
        // To ensure only one instance is created
        if (instance == null) {
            instance = new PrebidProvider();
        }
        return instance;
    }


}
