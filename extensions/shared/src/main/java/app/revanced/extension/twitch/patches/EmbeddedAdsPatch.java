package app.revanced.extension.twitch.patches;

import app.revanced.extension.twitch.api.RequestInterceptor;

@SuppressWarnings("unused")
public class EmbeddedAdsPatch {
    public static RequestInterceptor createRequestInterceptor() {
        return new RequestInterceptor();
    }
}