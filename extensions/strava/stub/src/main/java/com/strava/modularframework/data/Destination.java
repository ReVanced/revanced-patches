package com.strava.modularframework.data;

import java.io.Serializable;
import java.util.Map;

public final class Destination implements Serializable {
    public enum DestinationType {
        // @SerializedName("client_destination")
        CLIENT_DESTINATION,
        // @SerializedName("network_request")
        NETWORK_REQUEST
    }

    private final String method;
    private final Destination next;
    private final Destination onSuccess;
    private final Map<String, String> params;
    private final DestinationType type;
    private final String url;

    public Destination(DestinationType destinationType,
                       String str,
                       Map<String, String> map,
                       String method,
                       Destination onSuccess,
                       Destination next) {
        this.type = destinationType;
        this.url = str;
        this.params = map;
        this.method = method;
        this.onSuccess = onSuccess;
        this.next = next;
    }

    public final String getMethod() {
        return method;
    }

    public final Destination getNext() {
        return next;
    }

    public final Destination getOnSuccess() {
        return onSuccess;
    }

    public final Map<String, String> getParams() {
        return params;
    }

    public final DestinationType getType() {
        return type;
    }

    public final String getUrl() {
        return url;
    }

    public final boolean hasValidClientDestination() {
        return false;
    }

    public final boolean hasValidNetworkRequest() {
        return false;
    }
}
