package com.liveramp.ats.sampleapp;

import org.prebid.mobile.ExternalUserId;
import org.prebid.mobile.PrebidMobile;

import java.util.ArrayList;
import java.util.HashMap;

// TODO:
public class PairIdentity {

    public void setPairIDForPartnerSDKs(String[] pairIdArr) {
        setPairIdsForPrebid(pairIdArr);
        setPairIdsForPubmaticOW(pairIdArr);
        setPairIdsForNimbus(pairIdArr);
    }


    public void setPairIdsForPrebid(String[] pairIds) {

        ArrayList<ExternalUserId> externalUserIdArray = new ArrayList<>();

        for (String pairId : pairIds) {
            externalUserIdArray.add(new ExternalUserId("google.com", pairId, 571187, null));
        }

        // ... other Ids here..
        PrebidMobile.setExternalUserIds(externalUserIdArray);
        // TODO: Do a sample Prebid ad request to validate
    }


    public void setPairIdsForPubmaticOW(String[] pairIds){

        // TODO: Pubmatic is working on supporting multiple.
        // In the interim - we can begin to provide the first pair ID.

//        POBExternalUserId adServerUserId = new POBExternalUserId("google.com", pairIds[0]);
//        OpenWrapSDK.addExternalUserId(adServerUserId);

        // TODO: Do a sample OpenWrap ad request to validate
    }


    // [Nimbus] Set the updated RampID envelope in Nimbus's SDK
    // https://developers.liveramp.com/authenticatedtraffic-api/docs/configure-programmatic-ad-solution#nimbus
    // https://docs.adsbynimbus.com/docs/sdk/android/identity-providers/liveramp#setup
    public void setPairIdsForNimbus(String[] pairIds){

//        HashMap extMap = new HashMap<String, String>();
//        extMap.put("rtiPartner", "idl");
//
//        NimbusAdManager.addExtendedId("liveramp.com", envelope, extMap);
        // TODO: Do a sample Nimbus ad request to validate
    }


    private static PairIdentity instance = null;

    private PairIdentity() {}

    public static PairIdentity getInstance()
    {
        // To ensure only one instance is created
        if (instance == null) {
            instance = new PairIdentity();
        }
        return instance;
    }



}
