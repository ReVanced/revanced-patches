package app.revanced.extension.spotify.misc.fix;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.*;
import android.webkit.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.spotify.UserAgent;

class WebApp {
    private static final String OPEN_SPOTIFY_COM = "open.spotify.com";
    private static final String OPEN_SPOTIFY_COM_URL = "https://" + OPEN_SPOTIFY_COM;
    private static final String OPEN_SPOTIFY_COM_PREFERENCES_URL = OPEN_SPOTIFY_COM_URL + "/preferences";
    private static final String ACCOUNTS_SPOTIFY_COM_LOGIN_URL = "https://accounts.spotify.com/login?continue=" +
            "https%3A%2F%2Fopen.spotify.com%2Fpreferences";

    private static final int GET_SESSION_TIMEOUT_SECONDS = 10;
    private static final String JAVASCRIPT_INTERFACE_NAME = "androidInterface";
    private static final String USER_AGENT = getWebUserAgent();

    /**
     * A session obtained from the webview after logging in or renewing the session.
     */
    @Nullable
    static volatile Session currentSession;

    static void login(Context context) {
        Logger.printInfo(() -> "Starting login");

        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        // Ensure that the keyboard does not cover the webview content.
        Window window = dialog.getWindow();
        //noinspection StatementWithEmptyBody
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
                v.setPadding(0, 0, 0, insets.getInsets(WindowInsets.Type.ime()).bottom);

                return WindowInsets.CONSUMED;
            });
        } else {
            // TODO: Implement for lower Android versions.
        }

        newWebView(
                // Can't use Utils.getContext() here, because autofill won't work.
                // See https://stackoverflow.com/a/79182053/11213244.
                context,
                new WebViewCallback() {
                    @Override
                    void onInitialized(WebView webView) {
                        // Ensure that cookies are cleared before loading the login page.
                        CookieManager.getInstance().removeAllCookies((anyRemoved) -> {
                            Logger.printInfo(() -> "Loading URL: " + ACCOUNTS_SPOTIFY_COM_LOGIN_URL);
                            webView.loadUrl(ACCOUNTS_SPOTIFY_COM_LOGIN_URL);
                        });

                        dialog.setCancelable(false);
                        dialog.setContentView(webView);
                        dialog.show();
                    }

                    @Override
                    void onLoggedIn(String cookies) {
                        Logger.printInfo(() -> "Received cookies from login: " + cookies);
                        dialog.dismiss();
                    }

                    @Override
                    void onReceivedSession(WebView webView, Session session) {
                        Logger.printInfo(() -> "Received session from login: " + session);
                        currentSession = session;
                        webView.stopLoading();
                        webView.destroy();
                    }
                }
        );
    }

    static void renewSession(String cookies) {
        Logger.printInfo(() -> "Renewing session with cookies: " + cookies);

        CountDownLatch getSessionLatch = new CountDownLatch(1);

        newWebView(
                Utils.getContext(),
                new WebViewCallback() {
                    @Override
                    public void onInitialized(WebView webView) {
                        Logger.printInfo(() -> "Loading URL: " + OPEN_SPOTIFY_COM_PREFERENCES_URL +
                                " with cookies: " + cookies);
                        setCookies(cookies);
                        webView.loadUrl(OPEN_SPOTIFY_COM_PREFERENCES_URL);
                    }

                    @Override
                    public void onReceivedSession(WebView webView, Session session) {
                        Logger.printInfo(() -> "Received session: " + session);
                        currentSession = session;
                        getSessionLatch.countDown();
                        webView.stopLoading();
                        webView.destroy();
                    }
                }
        );

        try {
            boolean isAcquired = getSessionLatch.await(GET_SESSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!isAcquired) {
                Logger.printException(() -> "Failed to retrieve session within " + GET_SESSION_TIMEOUT_SECONDS + " seconds");
            } else {
                Logger.printInfo(() -> "Session retrieved successfully");
            }
        } catch (InterruptedException e) {
            Logger.printException(() -> "Failed to wait for session retrieval", e);
            Thread.currentThread().interrupt();
        }
    }

    abstract static class WebViewCallback {
        void onInitialized(WebView webView) {
        }

        void onLoggedIn(String cookies) {
        }

        void onReceivedSession(WebView webView, Session session) {
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private static void newWebView(
            Context context,
            WebViewCallback webViewCallback
    ) {
        Utils.runOnMainThreadNowOrLater(() -> {
            WebView webView = new WebView(context);
            WebSettings settings = webView.getSettings();
            settings.setDomStorageEnabled(true);
            settings.setJavaScriptEnabled(true);
            settings.setUserAgentString(USER_AGENT);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    if (OPEN_SPOTIFY_COM.equals(request.getUrl().getHost())) {
                        webViewCallback.onLoggedIn(getCurrentCookies());
                    }

                    return super.shouldInterceptRequest(view, request);
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    Logger.printInfo(() -> "Page started loading: " + url);

                    if (!url.contains(OPEN_SPOTIFY_COM_URL)) {
                        return;
                    }

                    Logger.printInfo(() -> "Evaluating script to get session on url: " + url);
                    String getSessionScript = "Object.defineProperty(Object.prototype, \"_username\", {" +
                            "   configurable: true," +
                            "   set(username) {" +
                            "       accessToken = this._builder?.accessToken;" +
                            "       if (accessToken) {" +
                            "           " + JAVASCRIPT_INTERFACE_NAME + ".getSession(username, accessToken);" +
                            "           delete Object.prototype._username;" +
                            "       }" +
                            "       " +
                            "       Object.defineProperty(this, \"_username\", {" +
                            "           configurable: true," +
                            "           enumerable: true," +
                            "           writable: true," +
                            "           value: username" +
                            "       })" +
                            "       " +
                            "   }" +
                            "});";

                    view.evaluateJavascript(getSessionScript, null);
                }
            });

            webView.addJavascriptInterface(new Object() {
                @SuppressWarnings("unused")
                @JavascriptInterface
                public void getSession(String username, String accessToken) {
                    Session session = new Session(username, accessToken, getCurrentCookies());
                    Utils.runOnMainThreadNowOrLater(() -> webViewCallback.onReceivedSession(webView, session));
                }
            }, JAVASCRIPT_INTERFACE_NAME);

            Logger.printInfo(() -> "WebView initialized with user agent: " + USER_AGENT);
            webViewCallback.onInitialized(webView);
        });
    }

    private static String getWebUserAgent() {
        String userAgentString = WebSettings.getDefaultUserAgent(Utils.getContext());
        try {
            return new UserAgent(userAgentString)
                    .withCommentReplaced("Android", "Windows NT 10.0; Win64; x64")
                    .withoutProduct("Mobile")
                    .toString();
        } catch (IllegalArgumentException e) {
            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36 Edge/137.0.0.0";
            String fallback = userAgentString;
            Logger.printException(() -> "Failed to get user agent, falling back to " + fallback, e);
        }

        return userAgentString;
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
