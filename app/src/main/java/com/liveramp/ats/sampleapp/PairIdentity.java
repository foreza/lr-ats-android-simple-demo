package com.liveramp.ats.sampleapp;

import com.adsbynimbus.Nimbus;
import com.adsbynimbus.NimbusAdManager;
import com.adsbynimbus.openrtb.request.EID;
import com.adsbynimbus.openrtb.request.UID;
import com.liveramp.ats.sampleapp.providers.*;
import com.pubmatic.sdk.common.OpenWrapSDK;
import com.pubmatic.sdk.common.models.POBExternalUserId;

import org.prebid.mobile.ExternalUserId;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.TargetingParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// TODO:
public class PairIdentity {

    public void setPairIDForPartnerSDKs(List<String> pairIdsList) {

        setPairIdsForPrebid(pairIdsList);
        setPairIdsForPubmaticOW(pairIdsList);
        setPairIdsForNimbus(pairIdsList);

    }


    public void setPairIdsForPrebid(List<String> pairIds) {

        // Using official Prebid SDK - storeExternalUserId will presently work
        for (String pairId : pairIds) {
            // TODO: There is a known (minor) issue in Prebid where you are allowed only one PairID per source
            TargetingParams.storeExternalUserId(new ExternalUserId("google.com", pairId, 571187, null));
        }

        // Presently, this will not -
//        ArrayList<ExternalUserId> externalUserIdArray = new ArrayList<>();
//        for (String pairId : pairIds) {
//             externalUserIdArray.add(new ExternalUserId("google.com", pairId, 571187, null));
//        }
//
//         PrebidMobile.setExternalUserIds(externalUserIdArray);
    }


    public void setPairIdsForPubmaticOW(List<String> pairIds){

        for (String pairId : pairIds) {
            POBExternalUserId tPairID = new POBExternalUserId("google.com", pairId);
            tPairID.setAtype(571187);
            OpenWrapSDK.addExternalUserId(tPairID);
        }

    }


    public void setPairIdsForNimbus(List<String> pairIds){

        Set<UID> tPairIDSet = new HashSet<UID>();

        for (String pairId : pairIds) {
            UID tPairID = new UID(pairId, 571187, new HashMap<>());
            tPairIDSet.add(tPairID);
        }

        EID nimbusPairEids = new EID("google.com", tPairIDSet);
        NimbusAdManager.getExtendedIds().add(nimbusPairEids);

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
