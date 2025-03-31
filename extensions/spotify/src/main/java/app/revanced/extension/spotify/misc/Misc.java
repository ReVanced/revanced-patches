package app.revanced.extension.spotify.misc;


@SuppressWarnings("unused")
public final class Misc {
    public static String removeStationString(String s) {
        return s.replace("spotify:station:", "spotify:");
    }
}
