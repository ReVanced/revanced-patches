package app.revanced.extension.boostforreddit;

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
        // Boost uses a static user agent for Redgifs API calls
        return "Boost";
    }

    public static OkHttpClient createClient() {
        return new OkHttpClient.Builder().addInterceptor(INSTANCE).build();
    }
}
