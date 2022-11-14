package com.google.ads.mediation.liveramp.ats;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.mediation.InitializationCompleteCallback;
import com.google.android.gms.ads.mediation.MediationConfiguration;
import com.google.android.gms.ads.mediation.VersionInfo;
import com.google.android.gms.ads.mediation.rtb.RtbAdapter;
import com.google.android.gms.ads.mediation.rtb.RtbSignalData;
import com.google.android.gms.ads.mediation.rtb.SignalCallbacks;
import com.liveramp.ats.*;
import com.liveramp.ats.callbacks.*;
import com.liveramp.ats.model.*;

import java.util.List;

public class LiveRampATSBidAdapter extends RtbAdapter {

    // https://developers.google.com/admob/android/custom-events/setup#initialize_the_adapter
    // Example: https://github.com/googleads/googleads-mobile-android-mediation/blob/master/ThirdPartyAdapters/pangle/pangle/src/main/java/com/google/ads/mediation/pangle/PangleMediationAdapter.java#L53
    public static final String LOGTAG = LiveRampATSBidAdapter.class.getSimpleName();

    @Override
    public void collectSignals(@NonNull RtbSignalData rtbSignalData, @NonNull SignalCallbacks signalCallbacks) {

        LRAtsManager.INSTANCE.getEnvelope(new LREmailIdentifier("jason.chiu@liveramp.com"), new LREnvelopeCallback() {
            @Override
            public void invoke(@Nullable Envelope envelope, @Nullable LRError lrError) {

                try {
                    if (lrError != null) {
                        throw new Exception(lrError.getMessage());
                    }

                    if (envelope.getEnvelope() != null) {
                        String signals = envelope.getEnvelope();
                        Log.i(LOGTAG, "LiveRamp ATS SDK signal collected: " + signals);
                        signalCallbacks.onSuccess(signals);
                    }

                } catch (Exception e) {
                    Log.e(LOGTAG, "An error occurred with envelope fetch:" + e.getLocalizedMessage());
                }
            }
        });


    }

    @NonNull
    @Override
    public VersionInfo getSDKVersionInfo() {
        return new VersionInfo(9, 1, 1);
    }

    @NonNull
    @Override
    public VersionInfo getVersionInfo() {
        return new VersionInfo(1, 0, 0);
    }

    @Override
    public void initialize(@NonNull Context context, @NonNull InitializationCompleteCallback initializationCompleteCallback, @NonNull List<MediationConfiguration> list) {

        String appID = "";

        for (MediationConfiguration config : list) {
            Bundle serverParameters = config.getServerParameters();
            appID = serverParameters.getString("appID");
        }

        Log.d(LOGTAG, "Initializing with PID..." + appID);

        if (appID.length() == 0) {
            Log.e(LOGTAG, "SDK failed to initialize: LiveRamp App ID not provided" );
            initializationCompleteCallback.onInitializationFailed("LiveRamp App ID not provided; SDK not initialized"); // TODO: make constants, stop duplication
            return;
        }

        // TODO: Make it safe to call LRAtsManager and safely terminate if LR isn't included
        try {
            LRAtsManager.INSTANCE.setHasConsentForNoLegislation(true);                          // TODO: Make this configurable
            LRAtsConfiguration config = new LRAtsConfiguration(appID, false, false);    // TODO: Make this configurable
            LRAtsManager.INSTANCE.initialize(config, new LRCompletionHandlerCallback() {
                @Override
                public void invoke(boolean success, @Nullable LRError lrError) {
                    if (success) {
                        Log.i(LOGTAG, "LiveRamp ATS SDK is initialized and ready for use!");
                        // Notify Google that we succeeded
                        initializationCompleteCallback.onInitializationSucceeded();
                    } else {
                        // TODO: SDK failed to initialize - handle this error.
                        Log.e(LOGTAG, "SDK failed to initialize: " + lrError.getMessage());
                        initializationCompleteCallback.onInitializationFailed(lrError.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            initializationCompleteCallback.onInitializationFailed(e.getMessage());
        }

    }

}