package com.liveramp.ats;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.mediation.InitializationCompleteCallback;
import com.google.android.gms.ads.mediation.MediationConfiguration;
import com.google.android.gms.ads.mediation.VersionInfo;
import com.google.android.gms.ads.mediation.rtb.RtbAdapter;
import com.google.android.gms.ads.mediation.rtb.RtbSignalData;
import com.google.android.gms.ads.mediation.rtb.SignalCallbacks;
import com.liveramp.ats.callbacks.*;
import com.liveramp.ats.model.*;

import java.util.List;

// TODO: Figure out the class name we will tell google
// From this point on, we cannot change it (not easily)
public class LRAtsMediationAdapter extends RtbAdapter {

    // For testing, make sure (until Google supports this in their backend)
    // To localmap the config json file to
    // https://googleads.g.doubleclick.net/getconfig/pubsetting?

    // https://developers.google.com/admob/android/custom-events/setup#initialize_the_adapter
    // Example: https://github.com/googleads/googleads-mobile-android-mediation/blob/master/ThirdPartyAdapters/pangle/pangle/src/main/java/com/google/ads/mediation/pangle/PangleMediationAdapter.java#L53
    public static final String LOGTAG = LRAtsMediationAdapter.class.getSimpleName();
    protected static VersionInfo versionInfo;


    protected static String LR_AppId;
    protected static Boolean LR_hasConsentForLegislation;
    protected static Boolean LR_enableTestMode;

    protected static LRIdentifierData identifierData;


    @NonNull
    public static void setLRATSInitParams(String liveRampATSAppId, Boolean hasConsentForLegislation, Boolean testModeEnabled) {
        LR_AppId = liveRampATSAppId;
        LR_hasConsentForLegislation = hasConsentForLegislation;
        LR_enableTestMode = testModeEnabled;
    }

    // Once this is set; on google's next collection - we will grab the envelope
    // THought: Don't fetch an envelope on this step as the SDK may not yet be initialized.
    public static void setLREmailIdentifier(String emailIdentifier) {

        // TODO: Any additional validation?
        if (emailIdentifier == null ||  emailIdentifier.length() <= 0){
            Log.e(LOGTAG, "(FAILED) setLREmailIdentifier: " + emailIdentifier);
            return;
        }

        Log.i(LOGTAG, "setLREmailIdentifier: " + emailIdentifier);
        identifierData = new LREmailIdentifier(emailIdentifier);
    }


    // Note: Should likely only be used in the US; heavily prefer email.
    public static void setLRPhoneIdentifier(String phoneIdentifier) {

        if (phoneIdentifier == null ||  phoneIdentifier.length() > 0){
            Log.e(LOGTAG, "(FAILED) setLRPhoneIdentifier: " + phoneIdentifier);
            return;
        }

        Log.i(LOGTAG, "setLRPhoneIdentifier: " + phoneIdentifier);
        identifierData = new LRPhoneIdentifier(phoneIdentifier);
    }


    // Do we even need this?
    public static void resetLRSDK(){
        LR_AppId = "";
        LR_hasConsentForLegislation = false; // Default
        LR_enableTestMode = false;
        identifierData = null;
        LRAtsManager.INSTANCE.resetSdk();
    }

    // TODO: We need to implement this so that when Google calls our adapter, we will respond either with an envelope or empty string
    // We might need a new method that can fetch an envelope without providing an identifier, like this POC's approach.
    // Similar to ATS.js - ats.retrieveEnvelope();
    // That way - collectSignals can simply call retrieveEnvelope().
    @Override
    public void collectSignals(@NonNull RtbSignalData rtbSignalData, @NonNull SignalCallbacks signalCallbacks) {

        // Suggestion: send an empty signal if we don't have any identifier data
        // this will at least allow us to troubleshoot more efficiently within the a3p param
        if (identifierData == null) {
            Log.e(LOGTAG, "LR ATS SDK Identifier data not set; using empty signal. To fix, set an identifier. Example: LRAtsMediationAdapter.setLREmailIdentifier(\"your@email.com\")");
            signalCallbacks.onSuccess("");
            return;
        }

        // For POC: We _could_ persist the identifier data as a static member and grab it
        // Hopefully it doesn't leak..
        LRAtsManager.INSTANCE.getEnvelope(identifierData, new LREnvelopeCallback() {
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
                    Log.e(LOGTAG, "An error occurred with envelope fetch: " + e.getLocalizedMessage());
                    // TODO: What error codes can we use? Are these arbitrary?
                    signalCallbacks.onFailure(new AdError(101, "Error with signal collection for LR", "LiveRamp"));
                }
            }
        });


    }

    // TODO: Should we just have this output the SDK version?
    @NonNull
    @Override
    public VersionInfo getSDKVersionInfo() {
        return versionInfo;
    }

    @NonNull
    @Override
    public VersionInfo getVersionInfo() {
        return versionInfo;
    }

    // TODO: Are there any other parameters we need to expose to google?
    // Google would ideally provide us all the params we need to init the SDK and can also initialize on our behalf
    @Override
    public void initialize(@NonNull Context context, @NonNull InitializationCompleteCallback initializationCompleteCallback, @NonNull List<MediationConfiguration> list) {

        if (LR_AppId == null || LR_AppId.length() == 0) {
            Log.e(LOGTAG, "AppID not provided; this is required for using LiveRamp's adapter." );
            initializationCompleteCallback.onInitializationFailed("App ID not provided; LR ATS SDK not initialized"); // TODO: make constants, stop duplication
            return;
        }

        Log.d(LOGTAG, "Initializing LRAtsMediationAdapter with" + LR_AppId);

        // Warn the user that some unexpected behavior will occur
        if (LR_hasConsentForLegislation == null || LR_hasConsentForLegislation == false) {
            Log.d(LOGTAG, "hasConsentForLegislation was not set, or was set to false. LR ATS SDK will check for consent strings from your CMP and will fail initialization if not found." );
        }

        if (LR_enableTestMode == true) {
            Log.d(LOGTAG, "LR ATS SDK will be running in test mode. If you're seeing this; all envelopes are TEST envelopes and will not be valid in bid requests.");
        }

        // Set the version string.
        try {
            String [] versionString = LRAtsManager.INSTANCE.getSdkVersion().split(".");
            versionInfo = new VersionInfo(Integer.parseInt(versionString[0]),
                    Integer.parseInt(versionString[1]),
                    Integer.parseInt(versionString[2]));
        } catch (Exception e){
            // TODO: nicely handle this. It shouldn't fail though.
            versionInfo = new VersionInfo(0,0,0);
        }

        // TODO: Make it safe to call LRAtsManager and safely terminate if LR isn't included
        try {
            LRAtsManager.INSTANCE.setHasConsentForNoLegislation(LR_hasConsentForLegislation);
            LRAtsConfiguration config = new LRAtsConfiguration(LR_AppId, LR_enableTestMode, false); // TODO: Expose logToFile as param? Will folks use it?
            LRAtsManager.INSTANCE.initialize(config, new LRCompletionHandlerCallback() {
                @Override
                public void invoke(boolean success, @Nullable LRError lrError) {
                    if (success) {
                        Log.i(LOGTAG, "LR ATS SDK is initialized and ready for use!");
                        // Notify Google that we succeeded
                        initializationCompleteCallback.onInitializationSucceeded();
                    } else {
                        // TODO: SDK failed to initialize - handle this error.
                        Log.e(LOGTAG, "LR ATS SDK failed to initialize: " + lrError.getMessage());
                        initializationCompleteCallback.onInitializationFailed(lrError.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            initializationCompleteCallback.onInitializationFailed(e.getMessage());
        }

    }

}