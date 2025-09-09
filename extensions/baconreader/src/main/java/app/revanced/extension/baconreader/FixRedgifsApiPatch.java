package app.revanced.extension.baconreader;

import app.revanced.extension.shared.fixes.redgifs.BaseFixRedgifsApiPatch;
import okhttp3.OkHttpClient;

/**
 * @noinspection unused
 */
public class FixRedgifsApiPatch extends BaseFixRedgifsApiPatch {
    static {
        INSTANCE = new FixRedgifsApiPatch();
    }

    public String getDefaultUserAgent() {
        // BaconReader uses a static user agent for Redgifs API calls
        return "BaconReader";
    }

    public static OkHttpClient install(OkHttpClient.Builder builder) {
        return builder.addInterceptor(INSTANCE).build();
    }
}
