package com.liveramp.ats.sampleapp;

//import org.prebid.mobile.ExternalUserId;
//import org.prebid.mobile.PrebidMobile;
//import com.adsbynimbus.NimbusAdManager;
//import com.inmobi.sdk.InMobiSdk;
//import com.mobilefuse.sdk.MobileFuseTargetingData;
//import com.pubmatic.sdk.common.OpenWrapSDK;
//import com.pubmatic.sdk.common.models.POBExternalUserId;

import com.adsbynimbus.NimbusAdManager;
import com.pubmatic.sdk.common.OpenWrapSDK;
import com.pubmatic.sdk.common.models.POBExternalUserId;

import org.prebid.mobile.ExternalUserId;
import org.prebid.mobile.TargetingParams;

import java.util.HashMap;

// TODO:
public class PartnerIdentity {

    public void setLREnvelopeForPartnerSDKs(String envelope) {

        setLREnvelopeForPrebid(envelope);
        setLREnvelopeForPubmaticOW(envelope);
        setLREnvelopeForNimbus(envelope);
        setLREnvelopeForInMobi(envelope);
        setLREnvelopeForMobileFuse(envelope);

        // More partners coming soon!
        // Note: Google Ad Manager is a separate workflow.

    }


    // [PREBID] Set the updated RampID envelope in Prebid SDK (or managed Prebid Partner)
    // https://developers.liveramp.com/authenticatedtraffic-api/docs/configure-programmatic-ad-solution#prebid-sdk
    // https://docs.prebid.org/prebid-mobile/pbm-api/android/pbm-targeting-params-android.html#user-identity
    // This ensures all subsequent ad requests to Prebid Server contain the RampID envelope.
    public void setLREnvelopeForPrebid(String envelope) {

        // Using official Prebid SDK - storeExternalUserId will presently work
        TargetingParams.storeExternalUserId(new ExternalUserId("liveramp.com", envelope, null, null));

        // Presently, this will not work
//        ArrayList<ExternalUserId> externalUserIdArray = new ArrayList<>();
//        externalUserIdArray.add(new ExternalUserId("liveramp.com", envelope, null, null));
//        PrebidMobile.setExternalUserIds(externalUserIdArray);
    }


    // [InMobi UnifId] Set the updated RampID envelope in InMobi's UnifID service
    // https://developers.liveramp.com/authenticatedtraffic-api/docs/configure-programmatic-ad-solution#inmobi
    // https://support.inmobi.com/monetize/data-identity/unifid/unifid-sdk-contract-specifications#unifid-api-specification
    // This ensures all subsequent ad requests to InMobi's exchange contain the RampID envelope.
    public void setLREnvelopeForInMobi(String envelope) {

//        JSONObject unifiedIds = new JSONObject();
//
//        try {
//            unifiedIds.put("liveramp.com", envelope);
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
//
//        InMobiSdk.setPublisherProvidedUnifiedId(unifiedIds);
        // TODO: Do a sample InMobi ad request to validate
    }


    // [Pubmatic OW] Set the updated RampID envelope in Pubmatic's OW server
    // https://developers.liveramp.com/authenticatedtraffic-api/docs/configure-programmatic-ad-solution#pubmatic
    // https://community.pubmatic.com/display/AOPO/Advanced+topics#Advancedtopics-Useridentity(datapartnerIDs)
    // This ensures all subsequent ad requests to Pubmatic OpenWrap contain the RampID envelope.
    public void setLREnvelopeForPubmaticOW(String envelope){

        POBExternalUserId identityEnvelope = new POBExternalUserId("liveramp.com", envelope);
        OpenWrapSDK.addExternalUserId(identityEnvelope);
        // TODO: Do a sample OpenWrap ad request to validate
    }


    // [Nimbus] Set the updated RampID envelope in Nimbus's SDK
    // https://developers.liveramp.com/authenticatedtraffic-api/docs/configure-programmatic-ad-solution#nimbus
    // https://docs.adsbynimbus.com/docs/sdk/android/identity-providers/liveramp#setup
    public void setLREnvelopeForNimbus(String envelope){

        HashMap<String, String> extMap = new HashMap<String, String>();
        extMap.put("rtiPartner", "idl");

        NimbusAdManager.addExtendedId("liveramp.com", envelope, extMap);
    }


    // [MobileFuse] Set the updated RampID envelope in MobileFuse's SDK
    // https://developers.liveramp.com/authenticatedtraffic-api/docs/configure-programmatic-ad-solution#mobilefuse
    // https://docs.mobilefuse.com/docs/leveraging-rampid-and-uid2#passing-in-a-liveramp-envelope-directly
    public void setLREnvelopeForMobileFuse(String envelope){

//        MobileFuseTargetingData.setLiveRampEnvelope(envelope);

        // TODO: Do a sample MF ad request to validate

    }


    private static PartnerIdentity instance = null;

    private PartnerIdentity() {}

    public static PartnerIdentity getInstance()
    {
        // To ensure only one instance is created
        if (instance == null) {
            instance = new PartnerIdentity();
        }
        return instance;
    }



}
