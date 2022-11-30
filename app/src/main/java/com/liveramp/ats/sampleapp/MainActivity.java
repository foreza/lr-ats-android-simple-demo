package com.liveramp.ats.sampleapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.ads.mediation.liveramp.ats.LiveRampATSBidAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.liveramp.ats.LRAtsManager;
import com.liveramp.ats.LRError;
import com.liveramp.ats.callbacks.LRCompletionHandlerCallback;
import com.liveramp.ats.callbacks.LREnvelopeCallback;
import com.liveramp.ats.model.Configuration;
import com.liveramp.ats.model.Envelope;
import com.liveramp.ats.model.LRAtsConfiguration;
import com.liveramp.ats.model.LREmailIdentifier;

import java.util.Map;


public class MainActivity extends AppCompatActivity {

    String LOGTAG = "LiveRamp ATS Sample";
    String appID = "e47b5b24-f041-4b9f-9467-4744df409e31"; // Sample App ID only; create your own!
    String env;

    Boolean notCheckingForCCPA = true;
    Boolean supportOtherGeos = true;

    TextView sdkVersionRef;
    TextView initStatusRef;
    TextView envelopeDisplayRef;
    TextView errMessageRef;
    EditText emailInputValue;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setViewListeners();
        clearErrorMessage();
        updateSDKVersionDisplay();
        updateSDKInitStatus();

        // Best practice: initialize the SDK before an authentication event has occurred.
        // initializeLiveRampATS();
        initAdMob();
    }


    private void initAdMob() {

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

                Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
                for (String adapterClass : statusMap.keySet()) {
                    AdapterStatus status = statusMap.get(adapterClass);
                    Log.d("MyApp", String.format(
                            "Adapter name: %s, Description: %s, Latency: %d",
                            adapterClass, status.getDescription(), status.getLatency()));
                }

                // Set the email for upcoming bid requests
                // LiveRampATSBidAdapter.setLREmailIdentifier("jason.chiu@liveramp.com");

            }
        });

    }


    private void doAdmobRequest() {
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }


    // Example on how to init LiveRamp
    private void initializeLiveRampATS(){


        if (notCheckingForCCPA || supportOtherGeos) {
            LRAtsManager.INSTANCE.setHasConsentForNoLegislation(true);
        }

        // For the 2nd param (boolean), set "true" to enable test mode
        // In test mode - the SDK will "simulate" envelopes.
        // Note: No network calls will actually be made!
        // Ensure you set this to true before testing in an acceptance/staging environment
        LRAtsConfiguration config = new LRAtsConfiguration(appID, false, false);

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

                    if (envelope.getEnvelope() != null) {
                        clearErrorMessage();
                        env = envelope.getEnvelope();
                        envelopeDisplayRef.setText(env);
                    }

                } catch (Exception e) {
                    Log.e(LOGTAG, "An error occurred with envelope fetch:" + e.getLocalizedMessage());
                    logErrorMessage(e.getLocalizedMessage());
                }
            }
        });
    }


    // EXAMPLE ONLY: Utility function to set SharedPreferences
    // Your CMP SDK should be taking care of this - the LiveRamp SDK only reads these values.
    private void setSharedPreferencesKeyForKeyValue(String key, String value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(key, value);
        editor.apply();
    }


    private void updateSDKVersionDisplay(){
        sdkVersionRef.setText(LRAtsManager.INSTANCE.getSdkVersion());
    }


    private void updateSDKInitStatus(){
        initStatusRef.setText(LRAtsManager.INSTANCE.getSdkStatus().toString());
    }


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

        Button btn_getGoogleAd = (Button) findViewById(R.id.btn_requestGoogleAd);

        // POC: Make google ad request
        btn_getGoogleAd.setOnClickListener(v -> {
            // Call for an ad after the AdMob/GMA SDK is initialized
            LiveRampATSBidAdapter.setLREmailIdentifier(emailInputValue.getText().toString());
            doAdmobRequest();
        });


        // Behavior for init SDK
        btn_initSDK.setOnClickListener(v -> {
            // setPlaceholderConsentExample();         // Ensure your consent values are set prior to initialization.
            initializeLiveRampATS();                // Initialize the SDK as early as possible after consent values have been stored.
        });

        // Behavior for fetch Envelope
        btn_fetchEnvelope.setOnClickListener(v -> {
            String email = emailInputValue.getText().toString();
            if (email.length() == 0) {
              email = "jason.chiu@liveramp.com";
            }
            retrieveEnvelopeForInput(email);
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

    }


    /*
        Example that sets the shared preferences for DEMO purposes.
        Your CMP SDK (LiveRamp also offers one!) will typically set this for you.

        ** WARNING **
        You have been warned. For the purposes of this sample code, we'll set it for you.
        Do not use this in production, or feel the wrath of massive fines.
     */

    String IABTCF_TC_STRING_KEY = "IABTCF_TCString";
    String IABTCF_VENDOR_CONSENTS_KEY = "IABTCF_VendorConsents";
    String IABTCF_PURPOSE_CONSENTS_KEY = "IABTCF_PurposeConsents";
    String IAB_CCPA_KEY = "IABUSPrivacy_String";


    private void setPlaceholderConsentExample() {
        setSharedPreferencesKeyForKeyValue(IABTCF_TC_STRING_KEY,
                "CPKZ42oPKZ5YtADABCENBlCgAP_AAAAAAAAAAwwAQAwgDDABADCAAA");
        setSharedPreferencesKeyForKeyValue(IABTCF_VENDOR_CONSENTS_KEY,
                "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001");
        setSharedPreferencesKeyForKeyValue(IABTCF_PURPOSE_CONSENTS_KEY,
                "1111111111");
        setSharedPreferencesKeyForKeyValue(IAB_CCPA_KEY,
                "1YNN");
    }


    private void logErrorMessage(String errMsg) {
        errMessageRef.setVisibility(View.VISIBLE);
        errMessageRef.setText("[ERROR]: " + errMsg);
    }


    private void clearErrorMessage() {
        errMessageRef.setVisibility(View.INVISIBLE);
        errMessageRef.setText("");
    }



}