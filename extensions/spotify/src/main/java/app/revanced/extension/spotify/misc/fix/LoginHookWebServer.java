package app.revanced.extension.spotify.misc.fix;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
import com.google.protobuf.ByteString;
import fi.iki.elonen.NanoHTTPD;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public class LoginHookWebServer {

    static final int SERVER_PORT = 4345;
    static final int BEARER_TOKEN_LATCH_TIMEOUT = 5;
    static final String WEBVIEW_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36 Edge/136.0.0.0";
    static final String OPEN_SPOTIFY_HOST = "open.spotify.com";
    static final String OPEN_SPOTIFY_URL = "https://" + OPEN_SPOTIFY_HOST;

    private static final class TokenAndUsername {

        public final String token;
        public final String username;

        private TokenAndUsername(String token, String username) {
            this.token = token;
            this.username = username;
        }
    }

    private final static class WebServer extends NanoHTTPD {

        public WebServer(int port) {
            super(port);
        }

        @NonNull
        @Override
        public Response serve(IHTTPSession session) {
            try {
                Logger.printInfo(() -> "Got request to " + session.getUri());

                int requestContentLength = Integer.parseInt(session.getHeaders().get("content-length"));
                byte[] requestBody = getRequestBody(session.getInputStream(), requestContentLength);

                LoginRequest loginRequest = LoginRequest.parseFrom(requestBody);

                String storedCredentialUsername = null;
                if (loginRequest.hasStoredCredential()) {
                    storedCredentialUsername = loginRequest.getStoredCredential().getUsername();
                }

                TokenAndUsername tokenAndUsername = getWebBearerTokenFromCookies(storedCredentialUsername);
                // Our latch timed out, let native handle the error and request again.
                if (tokenAndUsername == null) {
                    return makeResponse(Response.Status.INTERNAL_ERROR, null);
                }

                // storedCredentials cannot be empty.
                ByteString storedCredentials = ByteString.fromHex("00");
                byte[] loginResponse = LoginResponse.newBuilder()
                        .setOk(LoginOk.newBuilder()
                                .setUsername(tokenAndUsername.username)
                                .setAccessToken(tokenAndUsername.token)
                                .setStoredCredential(storedCredentials)
                                .setAccessTokenExpiresIn(3600)
                                .build())
                        .build().toByteArray();

                Logger.printInfo(() -> "Sending LoginResponse with web bearer token back to native");
                return makeResponse(Response.Status.OK, loginResponse);
            } catch (Exception ex) {
                Logger.printException(() -> "serve failure", ex);
            }

            return makeResponse(Response.Status.INTERNAL_ERROR, null);
        }

        @NonNull
        private static byte[] getRequestBody(@NonNull InputStream inputStream, int contentLength) throws IOException {
            byte[] requestBody = new byte[contentLength];
            int bytesRead = inputStream.read(requestBody, 0, contentLength);

            return requestBody;
        }

        private static Response makeResponse(Response.IStatus status, byte[] responseBody) {
            if (responseBody != null) {
                ByteArrayInputStream responseStream = new ByteArrayInputStream(responseBody);
                return newFixedLengthResponse(status, "application/x-protobuf", responseStream, responseBody.length);
            }
            return newFixedLengthResponse(status, "application/x-protobuf", null);
        }
    }

    private static void setCookies(String username, String cookies) {
        SharedPreferences sharedPreferences =
                Utils.getContext().getSharedPreferences("revanced", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("revanced_cookies_" + username, cookies);
        editor.apply();
    }

    private static String getCookies(String username) {
        SharedPreferences sharedPreferences =
                Utils.getContext().getSharedPreferences("revanced", Context.MODE_PRIVATE);

        return sharedPreferences.getString("revanced_cookies_" + username, null);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private static TokenAndUsername getWebBearerTokenFromCookies(String storedAccountUsername) {
        AtomicReference<String> webBearerToken = new AtomicReference<>();
        AtomicReference<String> accountUsername = new AtomicReference<>();

        AtomicReference<WebView> webViewRef = new AtomicReference<>();
        AtomicBoolean webViewKilled = new AtomicBoolean(false);

        CountDownLatch countDownLatch = new CountDownLatch(1);

        Utils.runOnMainThread(() -> {
            WebView webView = getWebView(Utils.getContext());
            webViewRef.set(webView);

            webView.setWebViewClient(new WebViewClient() {
                private boolean injectedCode;

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    Logger.printInfo(() -> "Page started " + url);
                    if (!injectedCode && url.contains("open.spotify.com")) {
                        String jsInjection = "Object.defineProperty(Object.prototype, \"_username\", {" +
                                "configurable: true," +
                                "set(username) {" +
                                "    if (username && this._builder != null && this._builder.accessToken) {" +
                                "        androidBridge.receiveTokenAndUsername(this._builder.accessToken, username);" +
                                "    }" +
                                "    Object.defineProperty(this, \"_username\", {" +
                                "        configurable: true," +
                                "        enumerable: true," +
                                "        writable: true," +
                                "        value: username" +
                                "    })" +
                                "}" +
                                "});";

                        view.evaluateJavascript(jsInjection, null);
                        injectedCode = true;
                    }
                }
            });

            webView.addJavascriptInterface(new Object() {
                @JavascriptInterface
                public void receiveTokenAndUsername(String token, String username) {
                    Logger.printInfo(() -> "Token received via JS: " + token + ", username: " + username);

                    Utils.runOnMainThread(() -> {
                        if (webViewKilled.get()) {
                            return;
                        }

                        webViewKilled.set(true);
                        WebView webView = webViewRef.get();
                        webView.stopLoading();
                        webView.destroy();
                    });

                    webBearerToken.set(token);
                    accountUsername.set(username);
                    countDownLatch.countDown();

                    String spotifyCookies = getSpotifyCookies();
                    setCookies(username, spotifyCookies);
                }
            }, "androidBridge");

            // If storedAccountUsername is null then this is the initial login for an account.
            // In this case, use the cookies already saved in the cookie manager.
            if (storedAccountUsername != null) {
                String storedCookies = getCookies(storedAccountUsername);

                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.setCookie(OPEN_SPOTIFY_URL, storedCookies);
            }

            CookieManager cookieManager = CookieManager.getInstance();
            String cookies = cookieManager.getCookie("https://open.spotify.com");
            Logger.printInfo(() -> "Current spotify cookies " + cookies);

            webView.loadUrl("https://open.spotify.com/preferences");
        });

        try {
            boolean success = countDownLatch.await(BEARER_TOKEN_LATCH_TIMEOUT, TimeUnit.SECONDS);
            if (!success) {
                Utils.runOnMainThread(() -> {
                    if (webViewKilled.get()) {
                        return;
                    }

                    webViewKilled.set(true);
                    WebView webView = webViewRef.get();
                    webView.stopLoading();
                    webView.destroy();
                });

                return null;
            }
        } catch (InterruptedException ignored) {
            return null;
        }

        return new TokenAndUsername(webBearerToken.get(), accountUsername.get());
    }

    @SuppressLint("SetJavaScriptEnabled")
    @NonNull
    private static WebView getWebView(Context context) {
        WebView webView = new WebView(context);
        WebSettings settings = webView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);
        settings.setUserAgentString(WEBVIEW_USER_AGENT);

        return webView;
    }

    private static String getSpotifyCookies() {
        CookieManager cookieManager = CookieManager.getInstance();
        return cookieManager.getCookie(OPEN_SPOTIFY_URL);
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

            String expiredCookie = cookieName + "=;domain=" + OPEN_SPOTIFY_HOST + ";path=/;Max-Age=0";
            cookieManager.setCookie(OPEN_SPOTIFY_HOST, expiredCookie);
        }

        cookieManager.flush();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private static void loginWithWebClient(Context context) {
        new Thread(() -> {
            CountDownLatch countDownLatch = new CountDownLatch(1);

            Utils.runOnMainThread(() -> {
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();

                WebView webView = getWebView(context);
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
                            Utils.runOnMainThread(webView::stopLoading);
                            alertDialog.dismiss();
                            countDownLatch.countDown();
                        }

                        return super.shouldInterceptRequest(view, request);
                    }
                });

                clearSpotifyCookies();

                Logger.printInfo(() -> "Loading url https://accounts.spotify.com/en/login?continue=https%3A%2F%2Fopen.spotify.com%2Fpreferences");
                webView.loadUrl("https://accounts.spotify.com/en/login?continue=https%3A%2F%2Fopen.spotify.com%2Fpreferences");
                alertDialog.setView(webView);
                Utils.showDialog((Activity) context, alertDialog, false, null);
            });

            try {
                Logger.printInfo(() -> "Waiting for login to be successfull");
                countDownLatch.await();
            } catch (InterruptedException ignored) {}
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

    /* public static void setLoginWebView(View inflatedView) {
        inflatedView.
    } */

    public static void startWebServer() {
        try {
            WebServer webServer = new WebServer(SERVER_PORT);
            webServer.start();
            Logger.printInfo(() -> "NanoHTTPD server running on http://127.0.0.1:" + SERVER_PORT);
        } catch (Exception ex) {
            Logger.printException(() -> "startWebServer failure", ex);
        }
    }
}
