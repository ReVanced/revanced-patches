package app.revanced.extension.spotify.misc.fix;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.Window;
import android.view.WindowInsets;
import android.webkit.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.spotify.UserAgent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static app.revanced.extension.spotify.misc.fix.Session.FAILED_TO_RENEW_SESSION;

class WebApp {
    private static final String OPEN_SPOTIFY_COM = "open.spotify.com";
    private static final String OPEN_SPOTIFY_COM_URL = "https://" + OPEN_SPOTIFY_COM;
    private static final String OPEN_SPOTIFY_COM_PREFERENCES_URL = OPEN_SPOTIFY_COM_URL + "/preferences";
    private static final String ACCOUNTS_SPOTIFY_COM_LOGIN_URL = "https://accounts.spotify.com/login?allow_password=1"
            + "&continue=https%3A%2F%2Fopen.spotify.com%2Fpreferences";

    private static final int GET_SESSION_TIMEOUT_SECONDS = 10;
    private static final String JAVASCRIPT_INTERFACE_NAME = "androidInterface";
    private static final String USER_AGENT = getWebUserAgent();

    /**
     * A session obtained from the webview after logging in.
     */
    @Nullable
    static volatile Session currentSession = null;

    /**
     * Current webview in use. Any use of the object must be done on the main thread.
     */
    @SuppressLint("StaticFieldLeak")
    private static volatile WebView currentWebView;

    interface NativeLoginHandler {
        void login();
    }

    static NativeLoginHandler nativeLoginHandler;

    static void launchLogin(Context context) {
        final Dialog dialog = newDialog(context);

        Utils.runOnBackgroundThread(() -> {
            Logger.printInfo(() -> "Launching login");

            // A session must be obtained from a login. Repeat until a session is acquired.
            boolean isAcquired = false;
            do {
                CountDownLatch onLoggedInLatch = new CountDownLatch(1);
                CountDownLatch getSessionLatch = new CountDownLatch(1);

                // Can't use Utils.getContext() here, because autofill won't work.
                // See https://stackoverflow.com/a/79182053/11213244.
                launchWebView(context, ACCOUNTS_SPOTIFY_COM_LOGIN_URL, new WebViewCallback() {
                    @Override
                    void onInitialized(WebView webView) {
                        super.onInitialized(webView);

                        dialog.setContentView(webView);
                        dialog.show();
                    }

                    @Override
                    void onLoggedIn(String cookies) {
                        onLoggedInLatch.countDown();
                    }

                    @Override
                    void onReceivedSession(Session session) {
                        super.onReceivedSession(session);

                        getSessionLatch.countDown();
                        dialog.dismiss();

                        try {
                            nativeLoginHandler.login();
                        } catch (Exception ex) {
                            Logger.printException(() -> "nativeLoginHandler failure", ex);
                        }
                    }
                });

                try {
                    // Wait indefinitely until the user logs in.
                    onLoggedInLatch.await();
                    // Wait until the session is received, or timeout.
                    isAcquired = getSessionLatch.await(GET_SESSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Logger.printException(() -> "Login interrupted", ex);
                    Thread.currentThread().interrupt();
                }
            } while (!isAcquired);
        });
    }

    static void renewSessionBlocking(String cookies) {
        Logger.printInfo(() -> "Renewing session with cookies: " + cookies);

        CountDownLatch getSessionLatch = new CountDownLatch(1);

        launchWebView(Utils.getContext(), OPEN_SPOTIFY_COM_PREFERENCES_URL, new WebViewCallback() {
            @Override
            public void onInitialized(WebView webView) {
                setCookies(cookies);
                super.onInitialized(webView);
            }

            public void onReceivedSession(Session session) {
                super.onReceivedSession(session);
                getSessionLatch.countDown();
            }
        });

        boolean isAcquired = false;
        try {
            isAcquired = getSessionLatch.await(GET_SESSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Logger.printException(() -> "Session renewal interrupted", ex);
            Thread.currentThread().interrupt();
        }

        if (!isAcquired) {
            Logger.printException(() -> "Failed to retrieve session within " + GET_SESSION_TIMEOUT_SECONDS + " seconds");
            currentSession = FAILED_TO_RENEW_SESSION;
            destructWebView();
        }
    }

    /**
     * All methods are called on the main thread.
     */
    abstract static class WebViewCallback {
        void onInitialized(WebView webView) {
            currentWebView = webView;
            currentSession = null; // Reset current session.
        }

        void onLoggedIn(String cookies) {
        }

        void onReceivedSession(Session session) {
            Logger.printInfo(() -> "Received session: " + session);
            currentSession = session;

            destructWebView();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private static void launchWebView(
            Context context,
            String initialUrl,
            WebViewCallback webViewCallback
    ) {
        Utils.runOnMainThreadNowOrLater(() -> {
            WebView webView = new WebView(context);
            WebSettings settings = webView.getSettings();
            settings.setDomStorageEnabled(true);
            settings.setJavaScriptEnabled(true);
            settings.setUserAgentString(USER_AGENT);

            // WebViewClient is always called off the main thread,
            // but callback interface methods are called on the main thread.
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    if (OPEN_SPOTIFY_COM.equals(request.getUrl().getHost())) {
                        Utils.runOnMainThread(() -> webViewCallback.onLoggedIn(getCurrentCookies()));
                    }

                    return super.shouldInterceptRequest(view, request);
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    Logger.printInfo(() -> "Page started loading: " + url);

                    if (!url.startsWith(OPEN_SPOTIFY_COM_URL)) {
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
                            "});" +
                            "if (new URLSearchParams(window.location.search).get('_authfailed') != null) {" +
                            "   " + JAVASCRIPT_INTERFACE_NAME + ".getSession(null, null);" +
                            "}";

                    view.evaluateJavascript(getSessionScript, null);
                }
            });

            webView.addJavascriptInterface(new Object() {
                @SuppressWarnings("unused")
                @JavascriptInterface
                public void getSession(String username, String accessToken) {
                    Session session = new Session(username, accessToken, getCurrentCookies());
                    Utils.runOnMainThread(() -> webViewCallback.onReceivedSession(session));
                }
            }, JAVASCRIPT_INTERFACE_NAME);

            CookieManager.getInstance().removeAllCookies((anyRemoved) -> {
                Logger.printInfo(() -> "Loading URL: " + initialUrl);
                webView.loadUrl(initialUrl);

                Logger.printInfo(() -> "WebView initialized with user agent: " + USER_AGENT);
                webViewCallback.onInitialized(webView);
            });
        });
    }

    private static void destructWebView() {
        Utils.runOnMainThreadNowOrLater(() -> {
            currentWebView.stopLoading();
            currentWebView.destroy();
            currentWebView = null;
        });
    }

    private static String getWebUserAgent() {
        String userAgentString = WebSettings.getDefaultUserAgent(Utils.getContext());
        try {
            return new UserAgent(userAgentString)
                    .withCommentReplaced("Android", "Windows NT 10.0; Win64; x64")
                    .withoutProduct("Mobile")
                    .toString();
        } catch (IllegalArgumentException ex) {
            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36 Edge/137.0.0.0";
            String fallback = userAgentString;
            Logger.printException(() -> "Failed to get user agent, falling back to " + fallback, ex);
        }

        return userAgentString;
    }

    @NonNull
    private static Dialog newDialog(Context context) {
        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setCancelable(false);

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
        return dialog;
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
