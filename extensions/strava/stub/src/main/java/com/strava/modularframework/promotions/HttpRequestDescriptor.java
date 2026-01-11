package com.strava.modularframework.promotions;

public final class HttpRequestDescriptor {
    private final String url;
    private final String method;

    public HttpRequestDescriptor(String url, String method) {
        this.url = url;
        this.method = method;
    }

    public final String getUrl() {
        return this.url;
    }

    public final String getMethod() {
        return this.method;
    }
}
