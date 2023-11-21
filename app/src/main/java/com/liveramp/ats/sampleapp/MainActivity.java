package com.liveramp.ats.sampleapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.liveramp.ats.LRAtsManager;
import com.liveramp.ats.LRError;
import com.liveramp.ats.callbacks.LRCompletionHandlerCallback;
import com.liveramp.ats.callbacks.LREnvelopeCallback;
import com.liveramp.ats.model.*;


public class MainActivity extends AppCompatActivity {

    String LOGTAG = "LiveRamp ATS Sample";

    // TODO: Replace the init appID with your own app ID
    // DO NOT use this in production - it will cause you monetization issues.
    String appID = "e47b5b24-f041-4b9f-9467-4744df409e31";


    String lr_envelope;
    String pairIds;

    Boolean notCheckingForCCPA = true;
    Boolean supportOtherGeos = true;

    TextView sdkVersionRef;
    TextView initStatusRef;
    TextView envelopeDisplayRef;
    TextView errMessageRef;
    EditText emailInputValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setViewListeners();
        clearErrorMessage();
        updateSDKVersionDisplay();
        updateSDKInitStatus();

        // Example only - make sure your CMP is initialized!
        setPlaceholderConsentExample();

        // Best practice: initialize the ATS SDK as early as possible.
        // When to do this: If you are leveraging ATS for non-google partners
        initializeLiveRampATS();

        // Best practice: configure the adapter before GMA SDK is initialized!
        // When to do this: If you are using Google GMA
        // Note: You do not have to set the email here; set it when you do have it
        AdManagerGMA.getInstance().configureLiveRampGMASignalProvider(appID);

        // Example: Typically, you also init GMA and Prebid SDK as early as possible
        AdManagerGMA.getInstance().initGMA(this);
        AdManagerPrebid.getInstance().initPrebid(this);
    }


    // Simulate the case where we just got an email
    // I want to set this in GMA as well as Prebid.
    private void simulateObtainedAuthentication(String email) {

        // Set this email within GMA SDK adapter
        AdManagerGMA.getInstance().setEmailForLiveRampGMASignalProvider(email);

        // Also kick off a call for the envelope
        retrieveEnvelopeForInput(email);
    }

    // Example on how to just initialize LiveRamp
    private void initializeLiveRampATS(){

        // Check to make sure LiveRamp is not already initializing
        if (LRAtsManager.INSTANCE.getSdkStatus() != SdkStatus.NOT_INITIALIZED) {
            Log.e(LOGTAG, "Detected that the ATS SDK is initialized, or in the process of initializing");
            return;
        }

        // You can initialize this on a background thread!
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if (notCheckingForCCPA || supportOtherGeos) {
                    LRAtsManager.INSTANCE.setHasConsentForNoLegislation(true);
                }

                // For the 2nd boolean param, set to "false" for production.
                // In test mode - the SDK will "simulate" envelopes.
                // Note: No network calls will actually be made in test mode
                // The last param can be set to null.

                LRAtsConfiguration config = new LRAtsConfiguration(appID, false, false, null);

                LRAtsManager.INSTANCE.initialize(config, new LRCompletionHandlerCallback() {
                    @Override
                    public void invoke(boolean success, @Nullable LRError lrError) {
                        if (success) {
                            Log.i(LOGTAG, "LiveRamp ATS SDK is initialized and ready for use!");
                            updateSDKInitStatus();
                            clearErrorMessage();
                        } else {
                            // TODO: SDK failed to initialize - handle this error.
                            Log.e(LOGTAG, "SDK failed to initialize.");
                            logErrorMessage(lrError.getMessage());
                        }
                    }
                });
            }
        };

        AsyncTask.execute(runnable);
    }


    // Example of Envelope fetch with email
    private void retrieveEnvelopeForInput(String email) {
        LRAtsManager.INSTANCE.getEnvelope(new LREmailIdentifier(email), new LREnvelopeCallback() {
            @Override
            public void invoke(@Nullable Envelope envelope, @Nullable LRError lrError) {

                try {
                    if (lrError != null) {
                        throw new Exception(lrError.getMessage());
                    }

                    envelopeDisplayRef.setText("");
                    String displayText = "";

                    if (envelope.getEnvelope() != null) {
                        lr_envelope = envelope.getEnvelope();

                        // TODO: Now, provide the lr_envelope value to your partner(s).
                        // This value expires - by calling `getEnvelope`, you will ensure this value remains relevant.
                        // Do NOT cache this value. It will not be valuable or useful!
                        // You should always be using the most up to date envelope with downstream partners.
                        // More documentation here: https://developers.liveramp.com/authenticatedtraffic-api/docs/configure-programmatic-ad-solution
                        AdManagerPrebid.getInstance().setRampIDEnvelopeForPrebid(lr_envelope);

                        displayText += "lr_envelope: " + formatStringForDisplay(lr_envelope);
                        clearErrorMessage();

                    }

                    // Example for PairIDs - ignore if you do not have this integration
                    if (envelope.getEnvelope25() != null) {
                        pairIds = envelope.getEnvelope25();

                        // self.setPairIDsForPartnerSDKs(envelope: pair_envelope)
                        displayText += "pairIds: " + formatStringForDisplay(pairIds) + "\n";
                    }

                    envelopeDisplayRef.setText(displayText);

                } catch (Exception e) {
                    Log.e(LOGTAG, "An error occurred with envelope fetch:" + e.getLocalizedMessage());
                    logErrorMessage(e.getLocalizedMessage());
                }
            }
        });
    }



    // Sample App Utility methods - can mostly disregard everything below this line
    // ======================================================================


     // Attach onclick listeners
    private void setViewListeners() {

        sdkVersionRef = (TextView) findViewById(R.id.tv_versionValue);
        initStatusRef = (TextView) findViewById(R.id.tv_initValue);
        envelopeDisplayRef = (TextView) findViewById(R.id.tv_envelopeValue);
        errMessageRef = (TextView) findViewById(R.id.tv_errorRegion);
        emailInputValue = (EditText) findViewById(R.id.editText_emailInput);

        Button btn_initSDK = (Button) findViewById(R.id.btn_initSDK);
        Button btn_fetchEnvelope = (Button) findViewById(R.id.btn_fetchEnvelope);
        Button btn_resetSDK = (Button) findViewById(R.id.btn_resetSDK);
        Button btn_clearAll = (Button) findViewById(R.id.btn_clearAll);

        Button btn_adReqGMA = (Button) findViewById(R.id.btn_sampleGMAReq);
        Button btn_adReqPrebid = (Button) findViewById(R.id.btn_samplePrebidReq);



        // Behavior for init SDK
        btn_initSDK.setOnClickListener(v -> {
            setPlaceholderConsentExample();         // Ensure your consent values are set prior to initialization.
            initializeLiveRampATS();                // Initialize the SDK as early as possible after consent values have been stored.
        });

        // Behavior for fetch Envelope
        btn_fetchEnvelope.setOnClickListener(v -> {
            String email = emailInputValue.getText().toString();
            if (email.length() == 0) {
              email = "sample.app@liveramp.com";    // For this demo, use a placeholder email if none is provided
            }
            simulateObtainedAuthentication(email);
        });

        // Behavior to reset SDK
        btn_resetSDK.setOnClickListener(v -> {
             LRAtsManager.INSTANCE.resetSdk();
            updateSDKInitStatus();
        });

        // Behavior to clear all input
        btn_clearAll.setOnClickListener(v -> {
            emailInputValue.setText("");
            envelopeDisplayRef.setText("");
            clearErrorMessage();
        });


        // Kick off Prebid Ad Request
        btn_adReqPrebid.setOnClickListener(v ->{
            AdManagerPrebid.getInstance().makePrebidAdRequest(this);
        });

        btn_adReqGMA.setOnClickListener(v -> {
            AdManagerGMA.getInstance().makeGMAAdRequest(this);
        });


    }


    // EXAMPLE ONLY: Example method that sets the shared preferences for DEMO purposes.
    // Do not use in production or feel the wrath of massive fines.
    // Your CMP sets these values for you - do not set them yourself.
    private void setPlaceholderConsentExample() {

        String IABTCF_TC_STRING_KEY = "IABTCF_TCString";
        String IABTCF_VENDOR_CONSENTS_KEY = "IABTCF_VendorConsents";
        String IABTCF_PURPOSE_CONSENTS_KEY = "IABTCF_PurposeConsents";
        String IAB_CCPA_KEY = "IABUSPrivacy_String";

        setSharedPreferencesKeyForKeyValue(IABTCF_TC_STRING_KEY,
                "CPKZ42oPKZ5YtADABCENBlCgAP_AAAAAAAAAAwwAQAwgDDABADCAAA");
        setSharedPreferencesKeyForKeyValue(IABTCF_VENDOR_CONSENTS_KEY,
                "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001");
        setSharedPreferencesKeyForKeyValue(IABTCF_PURPOSE_CONSENTS_KEY,
                "1111111111");
        setSharedPreferencesKeyForKeyValue(IAB_CCPA_KEY,
                "1YNY");
    }


    // EXAMPLE ONLY: Utility function to set SharedPreferences
    // Your CMP SDK should be taking care of this - the LiveRamp SDK reads these values.
    private void setSharedPreferencesKeyForKeyValue(String key, String value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(key, value);
        editor.apply();
    }


    private void logErrorMessage(String errMsg) {
        errMessageRef.setVisibility(View.VISIBLE);
        errMessageRef.setText("[ERROR]: " + errMsg);
    }


    private void clearErrorMessage() {
        errMessageRef.setVisibility(View.INVISIBLE);
        errMessageRef.setText("");
    }


    private void updateSDKVersionDisplay(){
        sdkVersionRef.setText(LRAtsManager.INSTANCE.getSdkVersion());
    }


    private void updateSDKInitStatus(){
        initStatusRef.setText(LRAtsManager.INSTANCE.getSdkStatus().toString());
    }

    private String formatStringForDisplay(String originalString) {
        if (originalString.length() > 100) {
            return originalString.substring(0,100) + "... +"
                    + (originalString.length()-100) + "\n";
        } else {
            return originalString;
        }
    }

}