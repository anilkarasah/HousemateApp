package com.example.housemateapp.entities;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Map;

public class MatchingRequest {
    public static final String COLLECTION_NAME = "matchingRequests";
    public static final String ID = "id";
    public static final String FROM_UID = "fromUid";
    public static final String TO_UID = "toUid";
    public static final String IS_NOTIFIED = "isNotified";
    public static final String IS_ACCEPTED = "isAccepted";

    public String id;

    public String fromUid;
    public User fromUser;

    public String toUid;
    public User toUser;

    public boolean isNotified;
    public boolean isAccepted;

    public MatchingRequest(String fromUid, String toUid, boolean isNotified, boolean isAccepted) {
        this.fromUid = fromUid;
        this.toUid = toUid;
        this.isNotified = isNotified;
        this.isAccepted = isAccepted;
    }

    public static MatchingRequest parseDocumentSnapshot(DocumentSnapshot documentSnapshot) {
        Map<String, Object> requestMap = documentSnapshot.getData();

        assert requestMap != null;
        String fromUid = requestMap.get(MatchingRequest.FROM_UID).toString();
        String toUid = requestMap.get(MatchingRequest.TO_UID).toString();
        boolean isNotified = Boolean.getBoolean(requestMap.get(MatchingRequest.IS_NOTIFIED).toString());
        boolean isAccepted = Boolean.getBoolean(requestMap.get(MatchingRequest.IS_ACCEPTED).toString());

        return new MatchingRequest(fromUid, toUid, isNotified, isAccepted);
    }
}
