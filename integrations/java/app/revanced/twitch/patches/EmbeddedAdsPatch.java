package app.revanced.twitch.patches;

import app.revanced.twitch.api.RequestInterceptor;

public class EmbeddedAdsPatch {
    public static RequestInterceptor createRequestInterceptor() {
        return new RequestInterceptor();
    }
}