package com.strava.modularframework.data;

public abstract class ListProperties {
    public abstract ListField getField(String key);

    // Added by patch.
    public abstract ListField getField$original(String key);
}
