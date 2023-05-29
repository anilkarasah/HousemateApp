package com.example.housemateapp.entities;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Map;

public class MatchingRequest {
    public static final String COLLECTION_NAME = "matchingRequests";
    public static final String ID = "id";
    public static final String FROM_UID = "fromUid";
    public static final String TO_UID = "toUid";
    public static final String IS_ACCEPTED = "isAccepted";

    public String id;

    public String fromUid;
    public User fromUser;

    public String toUid;
    public User toUser;

    public boolean fromCurrentUser;
    public int isAccepted;

    public RequestType requestType;

    public MatchingRequest(String fromUid, String toUid, int isAccepted, String uid) {
        this.id = uid;
        this.fromUid = fromUid;
        this.toUid = toUid;
        this.isAccepted = isAccepted;

        this.fromCurrentUser = fromUid.equals(uid);

        if (isAccepted != 0) {
            this.requestType = RequestType.Complete;
        } else {
            this.requestType = this.fromCurrentUser
                ? RequestType.Sent
                : RequestType.Waiting;
        }
    }

    public static MatchingRequest parseDocumentSnapshot(DocumentSnapshot documentSnapshot, String uid) {
        Map<String, Object> requestMap = documentSnapshot.getData();

        assert requestMap != null;
        String fromUid = requestMap.get(MatchingRequest.FROM_UID).toString();
        String toUid = requestMap.get(MatchingRequest.TO_UID).toString();

        int isAccepted = 0;
        Object isAcceptedObject = requestMap.get(MatchingRequest.IS_ACCEPTED);
        if (isAcceptedObject != null) {
            isAccepted = Integer.parseInt(isAcceptedObject.toString());
        }

        return new MatchingRequest(fromUid, toUid, isAccepted, uid);
    }

    @Override
    public String toString() {
        return "MatchingRequest{" +
            "id='" + id + '\'' +
            ", fromUid='" + fromUid + '\'' +
            ",\nfromUser=" + fromUser +
            ", toUid='" + toUid + '\'' +
            ",\ntoUser=" + toUser +
            ", fromCurrentUser=" + fromCurrentUser +
            ", isAccepted=" + isAccepted +
            ", requestType=" + requestType +
            '}';
    }
}
