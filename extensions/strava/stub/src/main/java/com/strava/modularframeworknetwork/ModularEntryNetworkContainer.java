package com.strava.modularframeworknetwork;

import com.strava.modularframework.data.ListProperties;
import com.strava.modularframework.data.ModularEntry;
import java.util.List;

public abstract class ModularEntryNetworkContainer {
    public abstract List<ModularEntry> getEntries();

    // Added by patch.
    public abstract List<ModularEntry> getEntries$original();

    public abstract String getPage();

    public abstract ListProperties getProperties();

    // Added by patch.
    public abstract ListProperties getProperties$original();
}
