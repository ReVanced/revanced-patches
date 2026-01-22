package com.strava.core.data;

public enum MediaType {
    PHOTO(1),
    VIDEO(2);

    private final int remoteValue;

    private MediaType(int remoteValue) {
        this.remoteValue = remoteValue;
    }

    public int getRemoteValue() {
        return remoteValue;
    }
}
