package com.strava.modularframework.data;

import java.util.List;

public abstract class ListField {
    public abstract Destination getDestination();

    public abstract String getElement();

    public abstract List<ListField> getFields();

    // Added by patch.
    public abstract List<ListField> getFields$original();
}
