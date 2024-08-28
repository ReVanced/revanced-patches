package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.youtube.requests.Route.Method.GET;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.requests.Requester;
import app.revanced.extension.youtube.requests.Route;

/**
 * Opens a dialog showing the links from {@link SocialLinksRoutes}.
 */
@SuppressWarnings({"unused", "deprecation"})
public class ReVancedAboutPreference extends Preference {

    private static String useNonBreakingHyphens(String text) {
        // Replace any dashes with non breaking dashes, so the English text 'pre-release'
        // and the dev release number does not break and cover two lines.
        return text.replace("-", "&#8209;"); // #8209 = non breaking hyphen.
    }

    private static String getColorHexString(int color) {
        return String.format("#%06X", (0x00FFFFFF & color));
    }

    protected boolean isDarkModeEnabled() {
        Configuration config = getContext().getResources().getConfiguration();
        final int currentNightMode = config.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * Subclasses can override this and provide a themed color.
     */
    protected int getLightColor() {
        return Color.WHITE;
    }

    /**
     * Subclasses can override this and provide a themed color.
     */
    protected int getDarkColor() {
        return Color.BLACK;
    }

    private String createDialogHtml(ReVancedSocialLink[] socialLinks) {
        final boolean isNetworkConnected = Utils.isNetworkConnected();

        StringBuilder builder = new StringBuilder();
        builder.append("<html>");
        builder.append("<body style=\"text-align: center; padding: 10px;\">");

        final boolean isDarkMode = isDarkModeEnabled();
        String backgroundColorHex = getColorHexString(isDarkMode ? getDarkColor() : getLightColor());
        String foregroundColorHex = getColorHexString(isDarkMode ? getLightColor() : getDarkColor());
        // Apply light/dark mode colors.
        builder.append(String.format(
                "<style> body { background-color: %s; color: %s; } a { color: %s; } </style>",
                backgroundColorHex, foregroundColorHex, foregroundColorHex));

        if (isNetworkConnected) {
            builder.append("<img style=\"width: 100px; height: 100px;\" "
                    // Hide the image if it does not load.
                    + "onerror=\"this.style.display='none';\" "
                    + "src=\"https://revanced.app/favicon.ico\" />");
        }

        String patchesVersion = Utils.getPatchesReleaseVersion();

        // Add the title.
        builder.append("<h1>")
                .append("ReVanced")
                .append("</h1>");

        builder.append("<p>")
                // Replace hyphens with non breaking dashes so the version number does not break lines.
                .append(useNonBreakingHyphens(str("revanced_settings_about_links_body", patchesVersion)))
                .append("</p>");

        // Add a disclaimer if using a dev release.
        if (patchesVersion.contains("dev")) {
            builder.append("<h3>")
                    // English text 'Pre-release' can break lines.
                    .append(useNonBreakingHyphens(str("revanced_settings_about_links_dev_header")))
                    .append("</h3>");

            builder.append("<p>")
                    .append(str("revanced_settings_about_links_dev_body"))
                    .append("</p>");
        }

        builder.append("<h2 style=\"margin-top: 30px;\">")
                .append(str("revanced_settings_about_links_header"))
                .append("</h2>");

        builder.append("<div>");
        for (ReVancedSocialLink social : socialLinks) {
            builder.append("<div style=\"margin-bottom: 20px;\">");
            builder.append(String.format("<a href=\"%s\">%s</a>", social.url, social.name));
            builder.append("</div>");
        }
        builder.append("</div>");

        builder.append("</body></html>");
        return builder.toString();
    }

    {
        setOnPreferenceClickListener(pref -> {
            // Show a progress spinner if the social links are not fetched yet.
            if (!SocialLinksRoutes.hasFetchedLinks() && Utils.isNetworkConnected()) {
                ProgressDialog progress = new ProgressDialog(getContext());
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.show();
                Utils.runOnBackgroundThread(() -> fetchLinksAndShowDialog(progress));
            } else {
                // No network call required and can run now.
                fetchLinksAndShowDialog(null);
            }

            return false;
        });
    }

    private void fetchLinksAndShowDialog(@Nullable ProgressDialog progress) {
        ReVancedSocialLink[] socialLinks = SocialLinksRoutes.fetchSocialLinks();
        String htmlDialog = createDialogHtml(socialLinks);

        Utils.runOnMainThreadNowOrLater(() -> {
            if (progress != null) {
                progress.dismiss();
            }
            new WebViewDialog(getContext(), htmlDialog).show();
        });
    }

    public ReVancedAboutPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public ReVancedAboutPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public ReVancedAboutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ReVancedAboutPreference(Context context) {
        super(context);
    }
}

/**
 * Displays html content as a dialog. Any links a user taps on are opened in an external browser.
 */
class WebViewDialog extends Dialog {

    private final String htmlContent;

    public WebViewDialog(@NonNull Context context, @NonNull String htmlContent) {
        super(context);
        this.htmlContent = htmlContent;
    }

    // JS required to hide any broken images. No remote javascript is ever loaded.
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        WebView webView = new WebView(getContext());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new OpenLinksExternallyWebClient());
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null);

        setContentView(webView);
    }

    private class OpenLinksExternallyWebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                getContext().startActivity(intent);
            } catch (Exception ex) {
                Logger.printException(() -> "Open link failure", ex);
            }
            // Dismiss the about dialog using a delay,
            // otherwise without a delay the UI looks hectic with the dialog dismissing
            // to show the settings while simultaneously a web browser is opening.
            Utils.runOnMainThreadDelayed(WebViewDialog.this::dismiss, 500);
            return true;
        }
    }
}

class ReVancedSocialLink {
    final boolean preferred;
    final String name;
    final String url;

    ReVancedSocialLink(JSONObject json) throws JSONException {
        this(json.getBoolean("preferred"),
                json.getString("name"),
                json.getString("url")
        );
    }

    ReVancedSocialLink(boolean preferred, String name, String url) {
        this.preferred = preferred;
        this.name = name;
        this.url = url;
    }

    @NonNull
    @Override
    public String toString() {
        return "ReVancedSocialLink{" +
                "preferred=" + preferred +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}

class SocialLinksRoutes {
    /**
     * Links to use if fetch links api call fails.
     */
    private static final ReVancedSocialLink[] NO_CONNECTION_STATIC_LINKS = {
            new ReVancedSocialLink(true, "ReVanced.app", "https://revanced.app")
    };

    private static final String SOCIAL_LINKS_PROVIDER = "https://api.revanced.app/v2";
    private static final Route.CompiledRoute GET_SOCIAL = new Route(GET, "/socials").compile();

    @Nullable
    private static volatile ReVancedSocialLink[] fetchedLinks;

    static boolean hasFetchedLinks() {
        return fetchedLinks != null;
    }

    static ReVancedSocialLink[] fetchSocialLinks() {
        try {
            if (hasFetchedLinks()) return fetchedLinks;

            // Check if there is no internet connection.
            if (!Utils.isNetworkConnected()) return NO_CONNECTION_STATIC_LINKS;

            HttpURLConnection connection = Requester.getConnectionFromCompiledRoute(SOCIAL_LINKS_PROVIDER, GET_SOCIAL);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            Logger.printDebug(() -> "Fetching social links from: " + connection.getURL());

            // Do not show an exception toast if the server is down
            final int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                Logger.printDebug(() -> "Failed to get social links.  Response code: " + responseCode);
                return NO_CONNECTION_STATIC_LINKS;
            }

            JSONObject json = Requester.parseJSONObjectAndDisconnect(connection);
            JSONArray socials = json.getJSONArray("socials");

            List<ReVancedSocialLink> links = new ArrayList<>();
            for (int i = 0, length = socials.length(); i < length; i++) {
                ReVancedSocialLink link = new ReVancedSocialLink(socials.getJSONObject(i));
                links.add(link);
            }
            Logger.printDebug(() -> "links: " + links);

            return fetchedLinks = links.toArray(new ReVancedSocialLink[0]);

        } catch (SocketTimeoutException ex) {
            Logger.printInfo(() -> "Could not fetch social links", ex); // No toast.
        } catch (JSONException ex) {
            Logger.printException(() -> "Could not parse about information", ex);
        } catch (Exception ex) {
            Logger.printException(() -> "Failed to get about information", ex);
        }

        return NO_CONNECTION_STATIC_LINKS;
    }
}
