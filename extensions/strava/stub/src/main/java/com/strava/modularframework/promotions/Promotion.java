package com.strava.modularframework.promotions;

public final class Promotion {
    private final HttpRequestDescriptor click;
    private final HttpRequestDescriptor impression;

    public Promotion(HttpRequestDescriptor httpRequestDescriptor,
                     HttpRequestDescriptor httpRequestDescriptor2) {
        this.click = httpRequestDescriptor;
        this.impression = httpRequestDescriptor2;
    }

    public final HttpRequestDescriptor getClick() {
        return this.click;
    }

    public final HttpRequestDescriptor getImpression() {
        return this.impression;
    }
}
