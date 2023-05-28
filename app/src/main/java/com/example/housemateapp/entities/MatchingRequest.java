package com.example.housemateapp.entities;

import androidx.annotation.Nullable;

public class MatchingRequest {
    public String fromUid;
    @Nullable
    public User fromUser;

    public String toUid;
    @Nullable
    public User toUser;

    public boolean isNotified;
    public boolean isAccepted;

    public MatchingRequest(String fromUid, String toUid, boolean isNotified, boolean isAccepted) {
        this.fromUid = fromUid;
        this.toUid = toUid;
        this.isNotified = isNotified;
        this.isAccepted = isAccepted;
    }
}
