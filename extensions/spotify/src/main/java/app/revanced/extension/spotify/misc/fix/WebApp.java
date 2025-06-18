package app.revanced.extension.spotify.misc.fix;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.webkit.*;
import androidx.annotation.NonNull;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

class WebApp {
    private static final String OPEN_SPOTIFY_COM = "open.spotify.com";
    private static final String OPEN_SPOTIFY_COM_URL = "https://" + OPEN_SPOTIFY_COM;
    private static final String OPEN_SPOTIFY_COM_PREFERENCES_URL = OPEN_SPOTIFY_COM_URL + "/preferences";
    private static final String ACCOUNTS_SPOTIFY_COM_LOGIN_URL = "https://accounts.spotify.com/login?continue=" +
            "https%3A%2F%2Fopen.spotify.com%2Fpreferences";

    private static final int GET_SESSION_TIMEOUT_SECONDS = 5;

    private static final String JAVASCRIPT_INTERFACE_NAME = "androidInterface";

    static Session currentSession;

    static void login(Context context) {
        Utils.runOnMainThreadNowOrLater(() -> {
            Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

            WebView webView = newWebView((String cookies) -> {
                Logger.printInfo(() -> "Received cookies from login: " + cookies);
                dialog.dismiss();
            }, (session) -> {
                Logger.printInfo(() -> "Received session from login: " + session);
                currentSession = session;
                session.save();
            });

            // Ensure that cookies are cleared before loading the login page.
            CookieManager.getInstance().removeAllCookies((anyRemoved) ->
                    webView.loadUrl(ACCOUNTS_SPOTIFY_COM_LOGIN_URL)
            );

            dialog.setCancelable(false);
            dialog.setContentView(webView);
            dialog.show();
        });
    }

    static void refreshSession(String cookies) {
        setCookies(cookies);

        Semaphore getSessionSemaphore = new Semaphore(0);

        Utils.runOnMainThreadNowOrLater(() -> {
            WebView webView = newWebView(null, session -> {
                Logger.printInfo(() -> "Received session: " + session);
                session.save();
                currentSession = session;
                getSessionSemaphore.release();
            });

            int attempts = 1;
            do {
                int attemptNumber = attempts;
                Logger.printInfo(() -> "Attempt " + attemptNumber + ": Getting session for "
                        + GET_SESSION_TIMEOUT_SECONDS + " seconds...");

                webView.stopLoading(); // Stop any previous loading.
                webView.loadUrl(OPEN_SPOTIFY_COM_PREFERENCES_URL);

                try {
                    boolean isAcquired = getSessionSemaphore.tryAcquire(GET_SESSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    if (isAcquired) break;

                } catch (InterruptedException e) {
                    Logger.printException(() -> "Interrupted while waiting for session", e);
                    break;
                }

            } while (attempts++ <= 3);

            webView.stopLoading();
            webView.destroy();
        });

        try {
            // At most 3 attempts * timeout duration.
            //noinspection ResultOfMethodCallIgnored
            getSessionSemaphore.tryAcquire(GET_SESSION_TIMEOUT_SECONDS * 3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Logger.printException(() -> "Interrupted while waiting for session from UI thread", e);
        }
    }

    interface HasLoggedInCallback {
        void onLoggedIn(String cookies);
    }

    interface HasReceivedSessionCallback {
        void onReceivedSession(Session session);
    }

    @NonNull
    @SuppressLint("SetJavaScriptEnabled")
    static WebView newWebView(
            HasLoggedInCallback hasLoggedInCallback,
            HasReceivedSessionCallback hasReceivedSessionCallback
    ) {
        WebView webView = new WebView(Utils.getContext());
        WebSettings settings = webView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);
        settings.setUserAgentString(getWebUserAgent());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (hasLoggedInCallback != null && request.getUrl().getHost().equals(OPEN_SPOTIFY_COM)) {
                    hasLoggedInCallback.onLoggedIn(getCurrentCookies());
                }

                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (!url.contains(OPEN_SPOTIFY_COM_PREFERENCES_URL) && !url.contains(OPEN_SPOTIFY_COM)) {
                    return;
                }

                Logger.printInfo(() -> "Evaluating scripts to get session from url " + url);
                String getSessionScript = "Object.defineProperty(Object.prototype, \"_username\", {" +
                        "   configurable: true," +
                        "   set(username) {" +
                        "       accessToken = this._builder?.accessToken;" +
                        "       if (accessToken) {" +
                        "           " + JAVASCRIPT_INTERFACE_NAME + ".getSession(username, accessToken);" +
                        "       }" +
                        "       " +
                        "       Object.defineProperty(this, \"_username\", {" +
                        "           configurable: true," +
                        "           enumerable: true," +
                        "           writable: true," +
                        "           value: username" +
                        "       })" +
                        "   }" +
                        "});";

                view.evaluateJavascript(getSessionScript, null);
            }
        });

        webView.addJavascriptInterface(new Object() {
            /** @noinspection unused*/
            @JavascriptInterface
            public void getSession(String username, String accessToken) {
                if (hasReceivedSessionCallback != null) {
                    Session session = new Session(username, accessToken, getCurrentCookies());
                    hasReceivedSessionCallback.onReceivedSession(session);
                }
            }
        }, JAVASCRIPT_INTERFACE_NAME);

        return webView;
    }

    static String getWebUserAgent() {
        String userAgent = WebSettings.getDefaultUserAgent(Utils.getContext());

        int index = userAgent.indexOf("linux");
        if (index != -1) {
            StringBuilder userAgentBuilder = new StringBuilder(userAgent);
            int start = userAgent.indexOf('(', index);
            int end = userAgent.indexOf(')', start);

            if (start != -1 && end != -1) {
                userAgentBuilder.replace(start + 1, end, "Windows NT 10.0; Win64; x64");
                String spoofedUserAgent = userAgentBuilder.toString();
                Logger.printInfo(() -> "Web user agent: " + spoofedUserAgent);
                return spoofedUserAgent;
            }
        }

        String fallbackUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36 Edge/137.0.0.0";
        Logger.printException(() -> "Failed to get web user agent, falling back to " + fallbackUserAgent);
        return fallbackUserAgent;
    }

    static String getCurrentCookies() {
        CookieManager cookieManager = CookieManager.getInstance();
        return cookieManager.getCookie(OPEN_SPOTIFY_COM_URL);
    }

    static void setCookies(String cookies) {
        CookieManager cookieManager = CookieManager.getInstance();

        String[] cookiesList = cookies.split(";");
        for (String cookie : cookiesList) {
            cookieManager.setCookie(OPEN_SPOTIFY_COM_URL, cookie);
        }
    }
}
