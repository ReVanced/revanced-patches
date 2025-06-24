package app.revanced.extension.spotify.misc.fix;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.spotify.UserAgent;

class WebApp {
    private static final String OPEN_SPOTIFY_COM = "open.spotify.com";
    private static final String OPEN_SPOTIFY_COM_URL = "https://" + OPEN_SPOTIFY_COM;
    private static final String OPEN_SPOTIFY_COM_PREFERENCES_URL = OPEN_SPOTIFY_COM_URL + "/preferences";
    private static final String ACCOUNTS_SPOTIFY_COM_LOGIN_URL = "https://accounts.spotify.com/login?continue=" +
            "https%3A%2F%2Fopen.spotify.com%2Fpreferences";

    private static final int GET_SESSION_TIMEOUT_SECONDS = 5;
    private static final String JAVASCRIPT_INTERFACE_NAME = "androidInterface";
    private static final String USER_AGENT = getWebUserAgent();

    /**
     * A session obtained from the webview after logging in or refreshing the session.
     */
    @Nullable
    static volatile Session currentSession;

    static void login(Context context) {
        Utils.runOnMainThreadNowOrLater(() -> {
            Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

            WebView webView = newWebView((String cookies) -> {
                Logger.printInfo(() -> "Received cookies from login: " + cookies);
                dialog.dismiss();
            }, (webView1, session) -> {
                Logger.printInfo(() -> "Received session from login: " + session);
                currentSession = session;

                Utils.runOnMainThreadNowOrLater(() -> {
                    webView1.stopLoading();
                    webView1.destroy();
                });
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

        CountDownLatch getSessionLatch = new CountDownLatch(1);
        AtomicReference<WebView> webViewReference = new AtomicReference<>(null);

        int attempts = 1;
        do {
            int attemptNumber = attempts;
            Logger.printInfo(() -> "Attempt " + attemptNumber + ": Getting session for "
                    + GET_SESSION_TIMEOUT_SECONDS + " seconds...");

            // Webview objects must always be used on main thread.
            Utils.runOnMainThread(() -> {
                WebView webView = newWebView(null, (webView1, session) -> {
                    Logger.printInfo(() -> "Received session: " + session);
                    currentSession = session;
                    getSessionLatch.countDown();
                });
                webViewReference.set(webView);
                webView.loadUrl(OPEN_SPOTIFY_COM_PREFERENCES_URL);
            });

            try {
                boolean isAcquired = getSessionLatch.await(GET_SESSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                // Cleanup attempt.
                Utils.runOnMainThread(() -> {
                    WebView webView = webViewReference.get();
                    if (webView != null) {
                        webView.stopLoading();
                        webView.destroy();
                    }
                });

                if (isAcquired) {
                    return;
                }
            } catch (InterruptedException ex) {
                Logger.printException(() -> "Interrupted while waiting for session", ex);
                Thread.currentThread().interrupt(); // Restore interrupt status flag.
                return;
            }
        } while (attempts++ <= 3);

        Logger.printException(() -> "Failed to get session after multiple attempts");
    }

    private interface HasLoggedInCallback {
        void onLoggedIn(String cookies);
    }

    private interface HasReceivedSessionCallback {
        void onReceivedSession(WebView webView, Session session);
    }

    @NonNull
    @SuppressLint("SetJavaScriptEnabled")
    private static WebView newWebView(
            HasLoggedInCallback hasLoggedInCallback,
            HasReceivedSessionCallback hasReceivedSessionCallback
    ) {
        WebView webView = new WebView(Utils.getContext());
        WebSettings settings = webView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);

        settings.setUserAgentString(USER_AGENT);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (hasLoggedInCallback != null && OPEN_SPOTIFY_COM.equals(request.getUrl().getHost())) {
                    hasLoggedInCallback.onLoggedIn(getCurrentCookies());
                }

                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (!url.contains(OPEN_SPOTIFY_COM_URL)) {
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
            @SuppressWarnings("unused")
            @JavascriptInterface
            public void getSession(String username, String accessToken) {
                if (hasReceivedSessionCallback != null) {
                    Session session = new Session(username, accessToken, getCurrentCookies());
                    hasReceivedSessionCallback.onReceivedSession(webView, session);
                }
            }
        }, JAVASCRIPT_INTERFACE_NAME);

        return webView;
    }

    @NonNull
    private static String getWebUserAgent() {
        String userAgentString = WebSettings.getDefaultUserAgent(Utils.getContext());

        try {
            return new UserAgent(userAgentString).removeProduct("Mobile").toString();
        } catch (IllegalArgumentException e) {
            Logger.printException(() -> "Failed to parse user agent: " + userAgentString, e);
        }

        String fallbackUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36 Edge/137.0.0.0";
        Logger.printException(() -> "Failed to get web user agent, falling back to " + fallbackUserAgent);
        return fallbackUserAgent;
    }

    private static String getCurrentCookies() {
        CookieManager cookieManager = CookieManager.getInstance();
        return cookieManager.getCookie(OPEN_SPOTIFY_COM_URL);
    }

    private static void setCookies(@NonNull String cookies) {
        CookieManager cookieManager = CookieManager.getInstance();

        String[] cookiesList = cookies.split(";");
        for (String cookie : cookiesList) {
            cookieManager.setCookie(OPEN_SPOTIFY_COM_URL, cookie);
        }
    }
}
