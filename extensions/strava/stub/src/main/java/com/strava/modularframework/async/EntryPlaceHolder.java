package com.strava.modularframework.async;

public final class EntryPlaceHolder {
    private final String url;
    private final boolean multipleEntries;
    private final Boolean shouldUseCacheOnReload;
    private final String loadingString;
    private final boolean useCustomLoadingState;
    private boolean stale;

    public EntryPlaceHolder(String url,
                            boolean multipleEntries,
                            Boolean shouldUseCacheOnReload,
                            String loadingString,
                            boolean useCustomLoadingState,
                            boolean stale) {
        this.url = url;
        this.multipleEntries = multipleEntries;
        this.shouldUseCacheOnReload = shouldUseCacheOnReload;
        this.loadingString = loadingString;
        this.useCustomLoadingState = useCustomLoadingState;
        this.stale = stale;
    }

    public final String getLoadingString$modular_framework_productionRelease() {
        return loadingString;
    }

    public final boolean getMultipleEntries$modular_framework_productionRelease() {
        return multipleEntries;
    }

    public final boolean getStale() {
        return stale;
    }

    public final String getUrl$modular_framework_productionRelease() {
        return url;
    }

    public final boolean getUseCustomLoadingState$modular_framework_productionRelease() {
        return useCustomLoadingState;
    }

    public final void setStale(boolean stale) {
        this.stale = stale;
    }

    public final boolean shouldUseCache() {
        return shouldUseCacheOnReload;
    }
}
