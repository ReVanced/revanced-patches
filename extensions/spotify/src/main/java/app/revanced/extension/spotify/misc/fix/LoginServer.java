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
import org.json.JSONException;
import org.json.JSONObject;

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

// TODO: Make this class package private once the open login web view code is refactored.
@SuppressWarnings("unused")
public class LoginServer extends NanoHTTPD {
    private static final String OPEN_SPOTIFY_COM_HOST = "open.spotify.com";
    private static final String OPEN_SPOTIFY_COM_URL = "https://" + OPEN_SPOTIFY_COM_HOST;
    private static final String OPEN_SPOTIFY_COM_PREFERENCES_URL = OPEN_SPOTIFY_COM_URL + "/preferences";
    private static final String ACCOUNTS_SPOTIFY_COM_LOGIN_URL = "https://accounts.spotify.com/login?continue=" +
            "https%3A%2F%2Fopen.spotify.com%2Fpreferences";

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
        boolean isInitialLogin = !loginRequest.hasStoredCredential();
        if (isInitialLogin) {
            Logger.printInfo(() -> "Initial login request received, getting session");

            Session session = newSession();
            if (session == null) {
                return LoginResponse.newBuilder().setError(LoginError.TRY_AGAIN_LATER).build();
            }

            return session.toLoginResponse();
        } else {
            Logger.printInfo(() -> "Login request with stored credential received, converting to session");
            Session session = Session.fromStoredCredential(loginRequest.getStoredCredential());
            if (session == null) {
                return LoginResponse.newBuilder()
                        .setError(LoginError.INVALID_CREDENTIALS)
                        .build();
            }

            if (!session.accessTokenExpired()) {
                Logger.printInfo(() -> "Returning valid access token");
                return session.toLoginResponse();
            }

            Logger.printInfo(() -> "Access token from stored credential has expired, + " +
                    "renewing with session cookies from stored credential");
            setCookies(session.cookies);

            session = newSession();
            if (session == null) {
                return LoginResponse.newBuilder().setError(LoginError.TRY_AGAIN_LATER).build();
            }

            if (session.username == null) {
                Logger.printInfo(() -> "No username received, meaning the session cookies " +
                                "from stored credential were invalid");
                // User is logged in but the session retrieved does contain the account username, which means
                // cookies are invalid or have expired. Return an invalid credentials login error.
                return LoginResponse.newBuilder()
                        .setError(LoginError.INVALID_CREDENTIALS)
                        .build();
            }

            return session.toLoginResponse();
        }
    }

    @Nullable
    private static Session newSession() {
        // In case the web view authentication with the cookies did not succeed, either because the cookies have expired,
        // or they are invalid, the username reference will be set to null as no account is authenticated.
        // For this reason, we need to also create a boolean to see whether we have set the username reference or not.
        AtomicReference<Boolean> usernameReferenceSet = new AtomicReference<>(false);
        AtomicReference<String> usernameReference = new AtomicReference<>(null);

        AtomicReference<String> accessTokenReference = new AtomicReference<>(null);
        AtomicReference<Long> expirationTimeReference = new AtomicReference<>(null);

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
                            "       const expirationTime = initialToken?.accessTokenExpirationTimestampMs;" +
                            "       if (accessToken && expirationTime) {" +
                            "           delete Object.prototype.initialToken;" +
                            "           " + JAVASCRIPT_INTERFACE_NAME + ".getTokenData(accessToken, expirationTime);" +
                            "       }" +
                            "       Object.defineProperty(this, \"initialToken\", {" +
                            "           configurable: true," +
                            "           enumerable: true," +
                            "           writable: true," +
                            "           value: initialToken" +
                            "       });" +
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
                            "       });" +
                            "   }" +
                            "});";

                    view.evaluateJavascript(getTokenDataScript, null);
                    view.evaluateJavascript(getUsernameScript, null);
                }
            });

            webView.addJavascriptInterface(new Object() {
                @JavascriptInterface
                public void getTokenData(String accessToken, long expirationTime) {
                    Logger.printInfo(() -> "Got access token " + accessToken + " and expiration time " + expirationTime);
                    accessTokenReference.set(accessToken);
                    expirationTimeReference.set(expirationTime);

                    if (usernameReferenceSet.get()) {
                        countDownLatch.get().countDown();
                    }
                }

                @JavascriptInterface
                public void getUsername(String username) {
                    Logger.printInfo(() -> "Got username " + username);
                    usernameReference.set(username);
                    usernameReferenceSet.set(true);

                    if (accessTokenReference.get() != null && expirationTimeReference.get() != null) {
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
                webView.loadUrl(OPEN_SPOTIFY_COM_PREFERENCES_URL);
            });

            try {
                isSessionSet = countDownLatch.get().await(GET_SESSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Logger.printException(() -> "Interrupted while waiting for session", e);
            }

            if (!isSessionSet && attempts < 3) {
                Logger.printInfo(() -> "Session not set, retrying...");
                countDownLatch.set(new CountDownLatch(1));
            } else {
                Utils.runOnMainThread(() -> {
                    WebView webView = webViewReference.get();
                    webView.stopLoading();
                    webView.destroy();
                });
            }
        } while (!isSessionSet && attempts++ < 3);

        if (!isSessionSet) {
            return null;
        }

        String username = usernameReference.get();
        String accessToken = accessTokenReference.get();
        long expirationTime = expirationTimeReference.get();
        String cookies = getCookies();

        Session session = new Session(username, accessToken, expirationTime, cookies);
        Logger.printInfo(() -> "Session with username " + session.username + ", access token " + session.accessToken +
                ", expiration time " + expirationTime + " and cookies " + cookies);

        return session;
    }

    private static String getCookies() {
        CookieManager cookieManager = CookieManager.getInstance();
        return cookieManager.getCookie(OPEN_SPOTIFY_COM_URL);
    }

    private static void setCookies(String cookies) {
        CookieManager cookieManager = CookieManager.getInstance();

        String[] cookiesList = cookies.split(";");
        for (String cookie : cookiesList) {
            cookieManager.setCookie(OPEN_SPOTIFY_COM_URL, cookie);
        }

        cookieManager.flush();
    }

    private static void clearCookies() {
        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = getCookies();

        if (cookies == null) {
            return;
        }

        String[] cookiesList = cookies.split(";");
        for (String cookie : cookiesList) {
            String cookieName = cookie.substring(0, cookie.indexOf("=")).trim();

            String expiredCookie = cookieName + "=; domain=" + OPEN_SPOTIFY_COM_URL + "; path=/; Max-Age=0";
            cookieManager.setCookie(OPEN_SPOTIFY_COM_URL, expiredCookie);
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
        settings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36 Edge/137.0.0.0");

        return webView;
    }

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
                            Logger.printInfo(() -> "Got authentication cookies " + getCookies());
                            Utils.runOnMainThreadNowOrLater(() -> {
                                webView.stopLoading();
                                webView.destroy();
                            });
                            dialog.dismiss();
                            countDownLatch.countDown();
                        }
                        return super.shouldInterceptRequest(view, request);
                    }
                });

                clearCookies();
                Logger.printInfo(() -> "Starting authentication web view with cookies " + getCookies());
                Logger.printInfo(() -> "Loading url " + ACCOUNTS_SPOTIFY_COM_LOGIN_URL);
                webView.loadUrl(ACCOUNTS_SPOTIFY_COM_LOGIN_URL);

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
         * Access token for this session.
         */
        public final String accessToken;
        /**
         * Session expiration timestamp in milliseconds.
         */
        public final Long expirationTime;
        /**
         * Authentication cookies for this session.
         */
        public final String cookies;

        /**
         * @param username       Username of the account. Null if this session does not have an authenticated user.
         * @param accessToken    Access token for this session.
         * @param expirationTime Access token expiration time in milliseconds.
         * @param cookies        Authentication cookies for this session.
         */
        private Session(@Nullable String username, String accessToken, long expirationTime, String cookies) {
            this.username = username;
            this.accessToken = accessToken;
            this.expirationTime = expirationTime;
            this.cookies = cookies;
        }

        /**
         * @return The number of milliseconds until the access token expires.
         */
        public long accessTokenExpiresInMillis() {
            long currentTime = System.currentTimeMillis();
            return expirationTime - currentTime;
        }

        /**
         * @return The number of seconds until the access token expires.
         */
        public int accessTokenExpiresInSeconds() {
            return (int) accessTokenExpiresInMillis() / 1000;
        }

        /**
         * @return True if the access token has expired, false otherwise.
         */
        public boolean accessTokenExpired() {
            return accessTokenExpiresInMillis() <= 0;
        }

        /**
         * @return A JSON string encoded session.
         */
        @Nullable
        public String toStoredCredentialJSONString() {
            String storedCredentialJson;
            try {
                storedCredentialJson = new JSONObject()
                        .put("cookies", cookies)
                        .put("accessToken", accessToken)
                        .put("expirationTime", expirationTime).toString();
            } catch (JSONException ex) {
                Logger.printException(() -> "Failed to convert session to stored credential", ex);
                return null;
            }

            return storedCredentialJson;
        }

        /**
         * @return A ByteString encoded session or an empty stored credential if this session could not be converted
         * to a JSON string.
         */
        public ByteString toStoredCredentialByteString() {
            String storedCredentialJSONString = toStoredCredentialJSONString();
            if (storedCredentialJSONString == null) {
                return ByteString.fromHex("00"); // Empty stored credential.
            }
            return ByteString.copyFrom(storedCredentialJSONString, StandardCharsets.UTF_8);
        }

        public LoginResponse toLoginResponse() {
            if (username == null) {
                throw new UnsupportedOperationException("Cannot convert an unauthenticated session into a LoginResponse");
            }

            return LoginResponse.newBuilder()
                    .setOk(LoginOk.newBuilder()
                            .setUsername(username)
                            .setAccessToken(accessToken)
                            .setStoredCredential(toStoredCredentialByteString())
                            .setAccessTokenExpiresIn(accessTokenExpiresInSeconds())
                            .build())
                    .build();
        }

        @Nullable
        public static Session fromStoredCredential(@NonNull StoredCredential storedCredential) {
            try {
                String jsonString = storedCredential.getData().toString(StandardCharsets.UTF_8);
                JSONObject storedCredentialJson = new JSONObject(jsonString);

                String username = storedCredential.getUsername();
                String cookies = storedCredentialJson.getString("cookies");
                String accessToken = storedCredentialJson.getString("accessToken");
                long expirationTime = storedCredentialJson.getLong("expirationTime");

                return new Session(username, accessToken, expirationTime, cookies);
            } catch (JSONException ex) {
                Logger.printException(() -> "Failed to convert stored credential to session", ex);
                return null;
            }
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
        };
    }
}
