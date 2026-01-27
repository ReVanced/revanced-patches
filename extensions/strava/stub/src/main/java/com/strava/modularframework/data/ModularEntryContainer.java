package com.strava.modularframework.data;

import java.util.List;

public abstract class ModularEntryContainer {
    public abstract List<ModularEntry> getEntries();

    // Added by patch.
    public abstract List<ModularEntry> getEntries$original();

    public abstract List<ModularMenuItem> getMenuItems();

    // Added by patch.
    public abstract List<ModularMenuItem> getMenuItems$original();

    public abstract String getPage();

    public abstract ListProperties getProperties();

    // Added by patch.
    public abstract ListProperties getProperties$original();
}
