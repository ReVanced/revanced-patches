package com.strava.modularframework.data;

import java.util.Map;

public abstract class MultiStateFieldDescriptor {
    public abstract Map<String, GenericModuleField> getStateMap();

    // Added by patch.
    public abstract Map<String, GenericModuleField> getStateMap$original();
}
