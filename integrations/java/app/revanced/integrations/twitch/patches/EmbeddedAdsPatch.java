package app.revanced.integrations.twitch.patches;

import app.revanced.integrations.twitch.api.RequestInterceptor;

@SuppressWarnings("unused")
public class EmbeddedAdsPatch {
    public static RequestInterceptor createRequestInterceptor() {
        return new RequestInterceptor();
    }
}