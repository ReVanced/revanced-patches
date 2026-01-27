package com.strava.modularframework.data;

import java.io.Serializable;

public abstract class GenericModuleField implements Serializable {
    public abstract Destination getDestination();

    public abstract String getElement();
}
