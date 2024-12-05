package app.revanced.extension.boostforreddit;

import com.rubenmayayo.reddit.ui.activities.WebViewActivity;

import app.revanced.extension.shared.fixes.slink.BaseFixSLinksPatch;

/**
 * @noinspection unused
 */
public class FixSLinksPatch extends BaseFixSLinksPatch {
    static {
        INSTANCE = new FixSLinksPatch();
    }

    private FixSLinksPatch() {
        webViewActivityClass = WebViewActivity.class;
    }

    public static boolean patchResolveSLink(String link) {
        return INSTANCE.resolveSLink(link);
    }

    public static void patchSetAccessToken(String accessToken) {
        INSTANCE.setAccessToken(accessToken);
    }
}
