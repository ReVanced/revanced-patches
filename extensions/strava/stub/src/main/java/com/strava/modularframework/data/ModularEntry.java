package com.strava.modularframework.data;

import java.util.List;

public interface ModularEntry {
    List<ModularEntry> getChildrenEntries();

    // Added by patch.
    List<ModularEntry> getChildrenEntries$original();

    Destination getDestination();

    String getElement();

    List<Module> getModules();

    // Added by patch.
    List<Module> getModules$original();

    String getPage();
}
