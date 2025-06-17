package app.revanced.extension.spotify.misc.fix;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.webkit.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.spotify.login5.v4.proto.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import fi.iki.elonen.NanoHTTPD;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static fi.iki.elonen.NanoHTTPD.Response.Status.INTERNAL_ERROR;

@SuppressWarnings("unused")
class LoginServer extends NanoHTTPD {
    private static final String OPEN_SPOTIFY_COM_HOST = "open.spotify.com";
    private static final String OPEN_SPOTIFY_COM_URL = "https://" + OPEN_SPOTIFY_COM_HOST;
    private static final String OPEN_SPOTIFY_PREFERENCES_URL = OPEN_SPOTIFY_COM_URL + "/preferences";
    private static final String SPOTIFY_LOGIN_URL =
            "https://accounts.spotify.com/en/login?continue=https%3A%2F%2Fopen.spotify.com%2Fpreferences";

    private static final String CONTENT_LENGTH_HEADER = "content-length";
    private static final String JAVASCRIPT_INTERFACE_NAME = "androidInterface";

    private static final int GET_SESSION_TIMEOUT_SECONDS = 5;

    private final static LoginResponse invalidCredentialsLoginError = LoginResponse.newBuilder()
            .setError(LoginError.INVALID_CREDENTIALS)
            .build();

    private final static LoginResponse tryAgainLoginError = LoginResponse.newBuilder()
            .setError(LoginError.INVALID_CREDENTIALS)
            .build();

    public LoginServer(int port) {
        super(port);
    }

    @NonNull
    @Override
    public Response serve(IHTTPSession request) {
        try {
            Logger.printInfo(() -> "Request URI: " + request.getUri());

            InputStream requestBodyInputStream = getRequestBodyInputStream(request);
            LoginRequest loginRequest = LoginRequest.parseFrom(requestBodyInputStream);

            MessageLite loginResponse = getLoginResponse(loginRequest);
            if (loginResponse == null) {
                return newResponse(INTERNAL_ERROR);
            }

            return newResponse(Response.Status.OK, loginResponse);
        } catch (Exception ex) {
            Logger.printException(() -> "serve failure", ex);
        }

        return newResponse(INTERNAL_ERROR);
    }

    @NonNull
    private static InputStream getRequestBodyInputStream(@NonNull IHTTPSession request) {
        long requestContentLength =
                Long.parseLong(Objects.requireNonNull(request.getHeaders().get(CONTENT_LENGTH_HEADER)));
        return limitedInputStream(request.getInputStream(), requestContentLength);
    }

    @NonNull
    @SuppressWarnings("SameParameterValue")
    private static Response newResponse(Response.Status status) {
        return newResponse(status, null);
    }

    @NonNull
    private static Response newResponse(Response.IStatus status, MessageLite messageLite) {
        if (messageLite == null) {
            return newFixedLengthResponse(status, "application/x-protobuf", null);
        }

        byte[] messageBytes = messageLite.toByteArray();
        InputStream stream = new ByteArrayInputStream(messageBytes);
        return newFixedLengthResponse(status, "application/x-protobuf", stream, messageBytes.length);
    }

    @Nullable
    private static LoginResponse getLoginResponse(@NonNull LoginRequest loginRequest) {
        boolean loggedIn = loginRequest.hasStoredCredential();
        if (loggedIn) {
            Session existingSession = Session.fromStoredCredential(loginRequest.getStoredCredential());
            if (existingSession != null && existingSession.isValid()) {
                Logger.printInfo(() -> "Using valid credential for session");
                return existingSession.toLoginResponse();
            }

            Logger.printInfo(() -> "Stored credential is too old, getting new session");
        } else {
            Logger.printInfo(() -> "Initial login request");
        }

        Session session = getSession();
        if (loggedIn) {
            // User is logged in but the session was not retrieved due to an unknown error.
            // Return null and answer the request with an internal error.
            if (session == null) {
                return null;
            }

            // User is logged in but the session retrieved does contain the account username, which means
            // cookies are invalid or have expired. Return an invalid credentials login error.
            if (session.username == null) {
                return invalidCredentialsLoginError;
            }
        }

        // User is not logged in and the session was not retrieved due to an unknown error.
        // Return a try again login error.
        if (session == null){
            return tryAgainLoginError;
        }

        return session.toLoginResponse();
    }

    @Nullable
    private static Session getSession() {
        // In case the web view authentication with the cookies did not succeed, either because the cookies have expired,
        // or they are not properly, the username reference will be set to null as no account is authenticated.
        // For this reason, we need to also create a boolean to see whether we have set the username reference or not.
        AtomicReference<Boolean> usernameReferenceSet = new AtomicReference<>(false);
        AtomicReference<String> usernameReference = new AtomicReference<>(null);

        AtomicReference<String> accessTokenReference = new AtomicReference<>(null);
        AtomicReference<Long> expirationTimestampMsReference = new AtomicReference<>(null);

        AtomicReference<CountDownLatch> countDownLatch = new AtomicReference<>(new CountDownLatch(1));
        AtomicReference<WebView> webViewReference = new AtomicReference<>(null);

        Utils.runOnMainThreadNowOrLater(() -> {
            WebView webView = newWebView(Utils.getContext());
            webViewReference.set(webView);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    Logger.printInfo(() -> "Evaluating scripts to get session on url " + url);

                    // Use Javascript Object prototype pollution to intercept when a class constructor sets the value
                    // for initialToken and _username.
                    // initialToken contains data about the current access token of the session.
                    // username contains the username of the current authenticated account, or null if no account
                    // is successfully authenticated.
                    // Once our interface functions are called, we delete the pollution to avoid them being called
                    // more than once by other class instances.

                    String getTokenDataScript = "Object.defineProperty(Object.prototype, \"initialToken\", {" +
                            "   configurable: true," +
                            "   set(initialToken) {" +
                            "       const accessToken = initialToken?.accessToken;" +
                            "       const expirationTimestampMs = initialToken?.accessTokenExpirationTimestampMs;" +
                            "       if (initialToken && accessToken && expirationTimestampMs) {" +
                            "           delete Object.prototype.initialToken;" +
                            "           " + JAVASCRIPT_INTERFACE_NAME + ".getTokenData(accessToken, expirationTimestampMs);" +
                            "       }" +
                            "       Object.defineProperty(this, \"initialToken\", {" +
                            "           configurable: true," +
                            "           enumerable: true," +
                            "           writable: true," +
                            "           value: initialToken" +
                            "       })" +
                            "   }" +
                            "});";

                    String getUsernameScript = "Object.defineProperty(Object.prototype, \"_username\", {" +
                            "   configurable: true," +
                            "   set(username) {" +
                            "       if (this._builder != null) {" +
                            "           delete Object.prototype._username;" +
                            "           " + JAVASCRIPT_INTERFACE_NAME + ".getUsername(username);" +
                            "       }" +
                            "       Object.defineProperty(this, \"_username\", {" +
                            "           configurable: true," +
                            "           enumerable: true," +
                            "           writable: true," +
                            "           value: username" +
                            "       })" +
                            "   }" +
                            "});";

                    view.evaluateJavascript(getTokenDataScript, null);
                    view.evaluateJavascript(getUsernameScript, null);
                }
            });

            webView.addJavascriptInterface(new Object() {
                @JavascriptInterface
                public void getTokenData(String accessToken, long expirationTimestampMs) {
                    accessTokenReference.set(accessToken);
                    expirationTimestampMsReference.set(expirationTimestampMs);

                    if (usernameReferenceSet.get()) {
                        countDownLatch.get().countDown();
                    }
                }

                @JavascriptInterface
                public void getUsername(String username) {
                    usernameReference.set(username);
                    usernameReferenceSet.set(true);

                    if (accessTokenReference.get() != null && expirationTimestampMsReference.get() != null) {
                        countDownLatch.get().countDown();
                    }
                }
            }, JAVASCRIPT_INTERFACE_NAME);
        });

        int attempts = 1;
        boolean isSessionSet = false;
        do {
            int finalAttempts = attempts;
            Logger.printInfo(() -> "Waiting to get session for " + GET_SESSION_TIMEOUT_SECONDS +
                    " seconds (Attempt " + finalAttempts + ")");

            Utils.runOnMainThread(() -> {
                WebView webView = webViewReference.get();
                webView.stopLoading(); // Stop any loading from previous attempts.
                webView.loadUrl(OPEN_SPOTIFY_PREFERENCES_URL);
            });

            try {
                isSessionSet = countDownLatch.get().await(GET_SESSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Logger.printException(() -> "Interrupted while waiting for session", e);
            }

            if (!isSessionSet) {
                Logger.printInfo(() -> "Session not set, retrying...");
                countDownLatch.set(new CountDownLatch(1));
            }

            Utils.runOnMainThread(() -> {
                WebView webView = webViewReference.get();
                webView.stopLoading();
                webView.destroy();
            });
        } while (!isSessionSet && attempts++ < 3);

        if (!isSessionSet) {
            return null;
        }

        String username = usernameReference.get();
        String accessToken = accessTokenReference.get();
        long expirationTimestampMs = expirationTimestampMsReference.get();
        String spotifyCookies = getSpotifyCookies();

        Session session = new Session(username, accessToken, expirationTimestampMs, spotifyCookies);
        Logger.printInfo(() -> "Session with username " + session.username + ", access token " + session.accessToken +
                ", expiration timestamp " + expirationTimestampMs + " and cookies " + spotifyCookies);

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

    @NonNull
    @SuppressLint("SetJavaScriptEnabled")
    private static WebView newWebView(Context context) {
        WebView webView = new WebView(Utils.getContext());
        WebSettings settings = webView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);

        return webView;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private static void loginWithWebClient(Context context) {
        new Thread(() -> {
            CountDownLatch countDownLatch = new CountDownLatch(1);

            Utils.runOnMainThreadNowOrLater(() -> {
                Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

                WebView webView = newWebView(context);

                webView.setWebViewClient(new WebViewClient() {
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

                Logger.printInfo(() -> "Loading url " + SPOTIFY_LOGIN_URL);
                webView.loadUrl(SPOTIFY_LOGIN_URL);

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
        /**
         * Username of the account. Null if this session does not have an authenticated user.
         */
        @Nullable
        public final String username;
        /**
         * Access token for this section.
         */
        public final String accessToken;
        /**
         * Session expiration timestamp in milliseconds.
         */
        public final Long expirationTimestampMs;
        /**
         * Authentication cookies for this section.
         */
        public final String cookies;

        /**
         * @param username Username of the account. Null if this session does not have an authenticated user.
         * @param accessToken Access token for this section.
         * @param expirationTimestampMs Session expiration timestamp in milliseconds.
         * @param cookies Authentication cookies for this section.
         */
        private Session(@Nullable String username, String accessToken, long expirationTimestampMs, String cookies) {
            this.username = username;
            this.accessToken = accessToken;
            this.expirationTimestampMs = expirationTimestampMs;
            this.cookies = cookies;
        }

        /**
         * @return The amount of seconds til this session access token is invalidated.
         */
        public int getSecondsRemaining() {
            long currentTime = System.currentTimeMillis();
            return (int) ((expirationTimestampMs - currentTime) / 1000);
        }

        /**
         * @return Whether this section access token is still valid.
         */
        public boolean isValid() {
            return getSecondsRemaining() > 0;
        }

        @NonNull
        public String toStoredCredential() {
            return "cookies=" + cookies +
                    ";accessToken=" + accessToken +
                    ";expirationTimestampMs=" + expirationTimestampMs;
        }

        @NonNull
        public ByteString toStoredCredentialByteString() {
            return ByteString.copyFrom(toStoredCredential(), StandardCharsets.UTF_8);
        }

        public LoginResponse toLoginResponse() {
            if (username == null) {
                throw new UnsupportedOperationException(
                        "Cannot convert a non authenticated session into a LoginResponse"
                );
            }

            return LoginResponse.newBuilder()
                    .setOk(LoginOk.newBuilder()
                            .setUsername(username)
                            .setAccessToken(accessToken)
                            .setStoredCredential(toStoredCredentialByteString())
                            .setAccessTokenExpiresIn(getSecondsRemaining())
                            .build())
                    .build();
        }

        @Nullable
        public static Session fromStoredCredential(@NonNull StoredCredential storedCredential) {
            String data = storedCredential.getData().toString(StandardCharsets.UTF_8);

            Map<String, String> credentialValues = new HashMap<>();
            List<String> REQUIRED_KEYS = Arrays.asList("cookies", "accessToken", "expirationTimestampMs");

            String[] pairs = data.split(";");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    credentialValues.put(key, value);
                }
            }

            for (String requiredKey : REQUIRED_KEYS) {
                if (!credentialValues.containsKey(requiredKey)) {
                    Logger.printInfo(() -> "Invalid stored credential format: " + data);
                    return null;
                }
            }

            String username = storedCredential.getUsername();
            String cookies =  credentialValues.get("cookies");
            String accessToken = credentialValues.get("accessToken");
            long expirationTimestampMs = Long.parseLong(credentialValues.get("expirationTimestampMs"));

            return new Session(username, accessToken, expirationTimestampMs, cookies);
        }
    }

    @NonNull
    private static InputStream limitedInputStream(InputStream inputStream, long contentLength) {
        return new FilterInputStream(inputStream) {
            private long remaining = contentLength;

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
                len = (int) Math.min(len, remaining);
                int result = super.read(b, off, len);
                if (result != -1) remaining -= result;
                return result;
            }

            @Override
            public long skip(long n) throws IOException {
                long bytesToSkip = Math.min(n, remaining);
                long result = super.skip(bytesToSkip);
                remaining -= result;
                return result;
            }
        };
    }
}
