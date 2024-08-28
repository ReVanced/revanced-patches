package org.chromium.net;

public abstract class UrlRequest {
    public abstract class Builder {
        public abstract Builder addHeader(String name, String value);
        public abstract UrlRequest build();
    }
}
