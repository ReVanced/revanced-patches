package org.chromium.net;

public abstract class ExperimentalUrlRequest {
    public abstract class Builder {
        public abstract ExperimentalUrlRequest.Builder addHeader(String name, String value);
        public abstract ExperimentalUrlRequest build();
    }
}
