package app.revanced.extension.spotify.misc.fix;

import androidx.annotation.NonNull;

class Constants {
    static final String CLIENT_TOKEN_API_PATH = "/v1/clienttoken";
    static final String CLIENT_TOKEN_API_URL = "https://clienttoken.spotify.com" + CLIENT_TOKEN_API_PATH;
    static final String IOS_USER_AGENT;

    static {
        String clientVersion = getClientVersion();
        int commitHashIndex = clientVersion.lastIndexOf(".");
        String version = clientVersion.substring(
                clientVersion.indexOf("-") + 1,
                clientVersion.lastIndexOf(".", commitHashIndex - 1)
        );

        IOS_USER_AGENT = "Spotify/" + version + " iOS/" + getSystemVersion() + " (" + getHardwareMachine() + ")";
    }

 
    // Modified by a patch. Do not touch.
    @NonNull
    static String getClientVersion() {
        return "";
    }

    // Modified by a patch. Do not touch.
    @NonNull
    static String getSystemVersion() {
        return "";
    }

    // Modified by a patch. Do not touch.
    @NonNull
    static String getHardwareMachine() {
        return "";
    }
}
