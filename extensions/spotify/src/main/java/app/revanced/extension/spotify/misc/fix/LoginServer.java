package app.revanced.extension.spotify.misc.fix;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.webkit.*;
import androidx.annotation.NonNull;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.spotify.login5.v4.proto.LoginOk;
import app.revanced.extension.spotify.login5.v4.proto.LoginRequest;
import app.revanced.extension.spotify.login5.v4.proto.LoginResponse;
import app.revanced.extension.spotify.login5.v4.proto.StoredCredential;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import fi.iki.elonen.NanoHTTPD;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static fi.iki.elonen.NanoHTTPD.Response.Status.INTERNAL_ERROR;

@SuppressWarnings("unused")
class LoginServer extends NanoHTTPD {
    private static final String OPEN_SPOTIFY_COM_HOST = "open.spotify.com";
    private static final String OPEN_SPOTIFY_COM_URL = "https://" + OPEN_SPOTIFY_COM_HOST;
    private static final String OPEN_SPOTIFY_PREFERENCES_URL = "https://open.spotify.com/preferences";

    private static final String CONTENT_LENGTH_HEADER = "content-length";
    private static final String JAVASCRIPT_INTERFACE_NAME = "androidInterface";

    private static final int GET_SESSION_TIMEOUT_SECONDS = 5;

    public LoginServer(int port) {
        super(port);
    }

    @NonNull
    @Override
    public Response serve(IHTTPSession request) {
        try {
            Logger.printDebug(() -> "Request URI: " + request.getUri());

            int requestContentLength =
                    Integer.parseInt(Objects.requireNonNull(request.getHeaders().get(CONTENT_LENGTH_HEADER)));

            InputStream inputStream = limitedInputStream(request.getInputStream(), requestContentLength);
            LoginRequest loginRequest = LoginRequest.parseFrom(inputStream);

            Session session = getSession(loginRequest);
            if (session == null) {
                Logger.printException(() -> "Failed to get session from web view");
                return newResponse(INTERNAL_ERROR);
            }

            MessageLite loginResponse = LoginResponse.newBuilder().setOk(
                    LoginOk.newBuilder()
                            .setUsername(session.username)
                            .setAccessToken(session.accessToken)
                            .setStoredCredential(ByteString.copyFrom(session.toSavedCredential(), StandardCharsets.UTF_8))
                            .setAccessTokenExpiresIn(3600)
                            .build()
            ).build();

            return newResponse(Response.Status.OK, loginResponse);
        } catch (Exception ex) {
            Logger.printException(() -> "serve failure", ex);
        }

        return newResponse(INTERNAL_ERROR);
    }


    private static Response newResponse(Response.Status status) {
        return newResponse(status, null);
    }

    private static Response newResponse(Response.IStatus status, MessageLite messageLite) {
        if (messageLite == null) return newFixedLengthResponse(status, "application/x-protobuf", null);

        byte[] messageBytes = messageLite.toByteArray();
        InputStream stream = new ByteArrayInputStream(messageBytes);
        return newFixedLengthResponse(status, "application/x-protobuf", stream, messageBytes.length);
    }

    private static void setCookies(String username, String cookies) {
        SharedPreferences sharedPreferences =
                Utils.getContext().getSharedPreferences("revanced", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("revanced_cookies_" + username, cookies);
        editor.apply();
    }


    @SuppressLint("SetJavaScriptEnabled")
    private static Session getSession(LoginRequest loginRequest) {
        boolean loggedIn = loginRequest.hasStoredCredential();
        if (loggedIn) {
            Session existingSession = Session.fromStoredCredential(loginRequest.getStoredCredential());
            if (existingSession != null && existingSession.isValid()) {
                Logger.printDebug(() -> "Using valid credential for session");

                return existingSession;
            }

            Logger.printDebug(() -> "Stored credential is too old, clearing cookies");
            CookieManager.getInstance().removeAllCookies(null);
        } else {
            Logger.printDebug(() -> "Initial login request");
        }

        AtomicReference<Session> sessionReference = new AtomicReference<>(null);
        AtomicReference<CountDownLatch> countDownLatch = new AtomicReference<>(new CountDownLatch(1));

        WebView webView = new WebView(Utils.getContext());

        Utils.runOnMainThreadNowOrLater(() -> {
            WebSettings settings = webView.getSettings();
            settings.setDomStorageEnabled(true);
            settings.setJavaScriptEnabled(true);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    Logger.printDebug(() -> "Evaluating script to get session on url " + url);

                    // Hook the "_username" property setter to get the session.
                    String getSessionScript = "Object.defineProperty(Object.prototype, \"_username\", {" +
                            "   configurable: true," +
                            "   set(username) {" +
                            "       accessToken = this._builder?.accessToken;" +
                            "       if (accessToken) {" +
                            "           androidInterface.getSession(username, accessToken);" +
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
                @JavascriptInterface
                public void getSession(String username, String accessToken) {
                    sessionReference.set(new Session(username, accessToken));
                    countDownLatch.get().countDown();
                }
            }, JAVASCRIPT_INTERFACE_NAME);

            int attempts = 1;
            boolean isSessionSet = false;
            do {
                int finalAttempts = attempts;
                Logger.printDebug(
                        () -> "Waiting to get session for " +
                                GET_SESSION_TIMEOUT_SECONDS +
                                " seconds (Attempt " + finalAttempts + ")"
                );

                webView.stopLoading(); // Stop any loading from previous attempts.
                webView.loadUrl(OPEN_SPOTIFY_PREFERENCES_URL);

                try {
                    isSessionSet = countDownLatch.get().await(GET_SESSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Logger.printException(() -> "Interrupted while waiting for session", e);
                }

                if (!isSessionSet) {
                    Logger.printDebug(() -> "Session not set, retrying...");
                    countDownLatch.set(new CountDownLatch(1));
                }
            } while (!isSessionSet && attempts++ < 3);

            webView.stopLoading();
            webView.destroy();
        });

        try {
            boolean success = countDownLatch.get().await(GET_SESSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!success) {
                Utils.runOnMainThreadNowOrLater(() -> {
                    webView.stopLoading();
                    webView.destroy();
                });

                return null;
            }
        } catch (InterruptedException ignored) {
            return null;
        }

        Session session = sessionReference.get();
        Logger.printDebug(
                () -> "Session with username " +
                        session.username + " and access token " +
                        session.accessToken
        );

        return session;
    }


    private static String getSpotifyCookies() {
        CookieManager cookieManager = CookieManager.getInstance();
        return cookieManager.getCookie(OPEN_SPOTIFY_COM_URL);
    }

    private static void clearSpotifyCookies() {
        CookieManager cookieManager = CookieManager.getInstance();
        String spotifyCookies = getSpotifyCookies();

        if (spotifyCookies == null) {
            return;
        }

        String[] cookieParts = spotifyCookies.split(";");
        for (String cookie : cookieParts) {
            String cookieName = cookie.substring(0, cookie.indexOf("=")).trim();

            String expiredCookie = cookieName + "=;domain=" + OPEN_SPOTIFY_COM_HOST + ";path=/;Max-Age=0";
            cookieManager.setCookie(OPEN_SPOTIFY_COM_HOST, expiredCookie);
        }

        cookieManager.flush();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private static void loginWithWebClient(Context context) {
        new Thread(() -> {
            CountDownLatch countDownLatch = new CountDownLatch(1);

            Utils.runOnMainThreadNowOrLater(() -> {
                Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

                WebView webView = newWebView();
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        Logger.printInfo(() -> "Page finished loading: " + url);
                    }

                    @Override
                    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                        if (request.getUrl().getHost().equals("open.spotify.com")) {
                            Logger.printInfo(() -> "Got authentication cookies");
                            Utils.runOnMainThreadNowOrLater(webView::stopLoading);
                            dialog.dismiss();
                            countDownLatch.countDown();
                        }
                        return super.shouldInterceptRequest(view, request);
                    }
                });

                clearSpotifyCookies();

                Logger.printInfo(() -> "Loading url https://accounts.spotify.com/en/login?continue=https%3A%2F%2Fopen.spotify.com%2Fpreferences");
                webView.loadUrl("https://accounts.spotify.com/en/login?continue=https%3A%2F%2Fopen.spotify.com%2Fpreferences");

                dialog.setContentView(webView);
                dialog.setCancelable(false);
                dialog.show();
            });

            try {
                Logger.printInfo(() -> "Waiting for login to be successful");
                countDownLatch.await();
            } catch (InterruptedException ignored) {
            }
        }).start();
    }

    public static void openLoginWebView(LayoutInflater layoutInflater) {
        try {
            Context context = layoutInflater.getContext();
            loginWithWebClient(context);
        } catch (Exception ex) {
            Logger.printException(() -> "openLoginWebView failure", ex);
        }
    }

    private static final class Session {
        public final String accessToken;
        public final String username;
        public final Long creationTime;

        private Session(String username, String accessToken) {
            this.accessToken = accessToken;
            this.username = username;
            this.creationTime = System.currentTimeMillis() / 1000; // Store creation time in seconds
        }

        private Session(String username, String accessToken, long creationTime) {
            this.username = username;
            this.accessToken = accessToken;
            this.creationTime = creationTime;
        }

        public boolean isValid() {
            long currentTime = System.currentTimeMillis() / 1000; // Current time in seconds
            return (currentTime - creationTime) < 3600; // Valid for 1 hour
        }

        public String toSavedCredential() {
            return accessToken + ";" + creationTime;
        }

        public static Session fromStoredCredential(StoredCredential storedCredential) {
            String data = storedCredential.getData().toString(StandardCharsets.UTF_8);
            String[] parts = data.split(";");
            if (parts.length != 2) {
                Logger.printDebug(() -> "Invalid stored credential format: " + data);
                return null;
            }

            String accessToken = parts[0];
            String username = storedCredential.getUsername();
            long creationTime = Long.parseLong(parts[1]);

            return new Session(accessToken, username, creationTime);
        }
    }

    private static InputStream limitedInputStream(InputStream inputStream, int contentLength) {
        return new FilterInputStream(inputStream) {
            private int remaining = contentLength;

            @Override
            public int read() throws IOException {
                if (remaining <= 0) return -1;
                int result = super.read();
                if (result != -1) remaining--;
                return result;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if (remaining <= 0) return -1;
                len = Math.min(len, remaining);
                int result = super.read(b, off, len);
                if (result != -1) remaining -= result;
                return result;
            }
        };
    }
}
