package app.revanced.extension.syncforreddit;


import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.fixes.redgifs.BaseFixRedgifsPatch;
import com.android.volley.Response;

/**
 * @noinspection unused
 */
public class FixRedgifsPatch extends BaseFixRedgifsPatch {
    static {
        INSTANCE = new FixRedgifsPatch();
    }

    public static void fetchVideoUrl(String link, boolean hd, Response.Listener<String> result) {
        Utils.runOnBackgroundThread(() -> {
            String url = INSTANCE.getVideoUrl(link, hd);
            if (url == null) return;
            Utils.runOnMainThread(() -> result.onResponse(url));
        });
    }
}
