package com.strava.modularframework.data;

import java.util.List;

public abstract class ModularComponent implements Module {
    @Override
    public abstract String getElement();

    @Override
    public abstract String getPage();

    public abstract List<Module> getSubmodules();

    // Added by patch.
    public abstract List<Module> getSubmodules$original();
}
