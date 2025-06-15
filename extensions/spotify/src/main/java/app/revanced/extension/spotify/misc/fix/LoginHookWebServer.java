package app.revanced.extension.spotify.misc.fix;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.webkit.*;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.spotify.login5.v4.proto.LoginOk;
import app.revanced.extension.spotify.login5.v4.proto.LoginResponse;
import com.google.protobuf.ByteString;
import fi.iki.elonen.NanoHTTPD;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public class LoginHookWebServer {
    static volatile String clientToken;
    static volatile String cookies;

    private final static class WebServer extends NanoHTTPD {

        public WebServer(int port) {
            super(port);
        }

        @Override
        public Response serve(IHTTPSession session) {
            try {
                Logger.printInfo(() -> "Got request " + session.getUri());

                String webBearerToken = getWebBearerTokenFromCookies(cookies);
                Logger.printInfo(() -> "Web bearer token " + webBearerToken);

                ByteString storedCredentials = ByteString.fromHex("416742685966456C6F625130523277326A7731734C30634E4A665F6C786D6D6F705A6A504233666142444E46556B717A52346D6A674452514C486C347133437A675441594A2D6F4F79496B48556F4C4167356266433449577366507A5937356A2D6E746E51596A73427866634A66652D4B6F7A5F674D5974446D67716C636542456E5F4365366972715945396F78416C3168464E39696838335A5F766D64333976652D5257367A31484C4F49387367527371506B616F6D4A6B7479416F6C6837");

                byte[] loginResponse = LoginResponse.newBuilder()
                        .setOk(LoginOk.newBuilder()
                                .setUsername("31bhxjkeeq3lxm6t6k4rsg5jpt6q")
                                .setAccessToken(webBearerToken)
                                .setStoredCredential(storedCredentials)
                                .setAccessTokenExpiresIn(3600)
                                .build())
                        .build().toByteArray();

                Logger.printInfo(() -> "Sending web bearer response back to native");
                ByteArrayInputStream responseStream = new ByteArrayInputStream(loginResponse);
                return newFixedLengthResponse(Response.Status.OK, "application/x-protobuf", responseStream, loginResponse.length);

                /* int contentLength = Integer.parseInt(session.getHeaders().get("content-length"));
                byte[] requestBody = getRequestBody(session.getInputStream(), contentLength);

                /* Response responseWithCookies = getResponseWithCookies(requestBody);
                if (responseWithCookies != null) {
                    return responseWithCookies;
                }

                URL url = new URL("https://login5.spotify.com" + session.getUri());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Client-Token", clientToken);
                connection.setRequestProperty("Content-Type", "application/x-protobuf");
                connection.setRequestProperty("Content-Length", String.valueOf(contentLength));
                connection.setRequestProperty("User-Agent", "Spotify/9.0.50.416 Android/33 (SM-S928B)");

                connection.setDoOutput(true);
                connection.getOutputStream().write(requestBody);

                int statusCode = connection.getResponseCode();
                Logger.printInfo(() -> "Status " + statusCode);

                @SuppressLint({"NewApi", "LocalSuppress"}) byte[] response = connection.getInputStream().readAllBytes();
                LoginResponse loginResponse = LoginResponse.parseFrom(response);

                if (!loginResponse.hasOk()) {
                    ByteArrayInputStream responseStream = new ByteArrayInputStream(response);
                    return newFixedLengthResponse(Response.Status.OK, "application/x-protobuf", responseStream, response.length);
                }

                LoginOk.Builder loginOk = loginResponse.getOk().toBuilder();
                String accessToken = loginResponse.getOk().getAccessToken();
                Logger.printInfo(() ->  "Android access token " + accessToken);

                String webBearerToken = transferSession(accessToken, clientToken);
                Logger.printInfo(() ->  "Web access token " + accessToken);

                loginOk.setAccessToken(webBearerToken);
                byte[] newLoginResponse = loginResponse.toBuilder()
                        .setOk(loginOk)
                        .build().toByteArray();

                ByteArrayInputStream newResponseStream = new ByteArrayInputStream(newLoginResponse);
                return newFixedLengthResponse(Response.Status.OK, "application/x-protobuf", newResponseStream, newLoginResponse.length); */
            } catch (Exception e) {
                Logger.printException(() -> "error", e);
            }

            return newFixedLengthResponse("ok");
        }

        private static void setCookies(SharedPreferences sharedPreferences, String username, String cookies) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("revanced_cookies_" + username, cookies);
            editor.apply();
        }

        private static String getCookies(SharedPreferences sharedPreferences, String username) {
            return sharedPreferences.getString("revanced_cookies_" + username, null);
        }

        private static byte[] getRequestBody(InputStream inputStream, int contentLength) throws IOException {
            byte[] requestBody = new byte[contentLength];
            int bytesRead = inputStream.read(requestBody, 0, contentLength);

            return requestBody;
        }

        /* private static Response getResponseWithCookies(byte[] requestBody) throws InvalidProtocolBufferException {
            LoginRequest loginRequest = LoginRequest.parseFrom(requestBody);
            if (loginRequest.hasStoredCredential()) {
                String username = loginRequest.getStoredCredential().getUsername();
                ByteString storedCredential = loginRequest.getStoredCredential().getData();
                int expiresIn = 3600;

                SharedPreferences sharedPreferences =
                        Utils.getContext().getSharedPreferences("revanced", Context.MODE_PRIVATE);
                String cookies = getCookies(sharedPreferences, "");
                if (cookies != null) {
                    String webBearerToken = getWebBearerTokenResponse(, cookies);

                    byte[] loginResponse = LoginResponse.newBuilder()
                            .setOk(LoginOk.newBuilder()
                                    .setUsername(username)
                                    .setAccessToken(webBearerToken)
                                    .setStoredCredential(storedCredential)
                                    .setAccessTokenExpiresIn(expiresIn)
                                    .build())
                            .build().toByteArray();

                    ByteArrayInputStream loginResponseStream = new ByteArrayInputStream(loginResponse);
                    return newFixedLengthResponse(Response.Status.OK, "application/x-protobuf", loginResponseStream, loginResponse.length);
                }
            }

            return null;
        } */

        @SuppressLint("SetJavaScriptEnabled")
        private static String getWebBearerTokenFromCookies(String cookies) {
            AtomicReference<String> webBearerTokenResponse = new AtomicReference<>();

            CountDownLatch latch = new CountDownLatch(1);

            Utils.runOnMainThread(() -> {
                WebView webView = new WebView(Utils.getContext());
                WebSettings settings = webView.getSettings();
                settings.setJavaScriptEnabled(true);
                settings.setDomStorageEnabled(true);
                settings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36 Edg/137.0.0.0");

                webView.setWebViewClient(new WebViewClient() {
                    private boolean patchedFetch;

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        Logger.printInfo(() -> "Url getWebBearerTokenFromCookies " + url);

                        // Hook into fetch requests to capture the Authorization header.
                        if (!patchedFetch && url.contains("open.spotify.com")) {
                            String jsHook = "(function() {" +
                                    "   const originalFetch = window.fetch;" +
                                    "   window.fetch = (input, init) => {" +
                                    "       const request = typeof input === 'string' ? new Request(input, init) : input;" +
                                    "       const header = request.headers?.get?.('Authorization') || (init?.headers?.Authorization);" +
                                    "       if (header) androidBridge.receiveToken(header, \"\");" +
                                    "       return originalFetch(input, init);" +
                                    "   };" +
                                    "})();";

                            view.evaluateJavascript(jsHook, null);
                            patchedFetch = true;
                        }
                    }
                });

                webView.addJavascriptInterface(new Object() {
                    @JavascriptInterface
                    public void receiveToken(String token, String username) {
                        Logger.printInfo(() -> "Token received via JS: " + token + ", username: " + username);

                        Utils.runOnMainThread(webView::stopLoading);
                        webBearerTokenResponse.set(token);
                        latch.countDown();

                        /* CookieManager cookieManager = CookieManager.getInstance();
                        String cookies = cookieManager.getCookie("https://open.spotify.com");
                        SharedPreferences sharedPreferences =
                                Utils.getContext().getSharedPreferences("revanced", Context.MODE_PRIVATE);
                        setCookies(sharedPreferences, username, cookies); */
                    }
                }, "androidBridge");

                CookieManager cookieManager = CookieManager.getInstance();
                Logger.printInfo(() -> "Setting cookies to " + cookies);
                String aacookies = cookieManager.getCookie("https://open.spotify.com");
                Logger.printInfo(() -> "Spotify cookies " + aacookies);
                webView.loadUrl("https://open.spotify.com/collection/tracks");
            });

            try {
                latch.await();
            } catch (InterruptedException ignored) {
            }

            return webBearerTokenResponse.get().replace("Bearer ", "");
        }

        private static String readConnectionResponse(HttpURLConnection connection) throws IOException {
            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return response.toString();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private static void getCookiesFromWebView(Context context) {
        new Thread(() -> {
            AtomicReference<String> cookiesAtomicReference = new AtomicReference<>();

            try {
                CountDownLatch latch = new CountDownLatch(1);
                Utils.runOnMainThread(() -> {
                    AlertDialog dialog = new AlertDialog.Builder(context).create();
                    Logger.printInfo(() -> "dialog created");

                    Logger.printInfo(() -> "HELLOOOOOOO");
                    try {
                        WebView webView = new WebView(context);
                        webView.setInitialScale(1);
                        WebSettings settings = webView.getSettings();
                        settings.setLoadWithOverviewMode(true);
                        settings.setUseWideViewPort(true);
                        settings.setJavaScriptEnabled(true);
                        settings.setDomStorageEnabled(true);
                        settings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36 Edg/137.0.0.0");


                        Logger.printInfo(() -> "setting webview client");
                        webView.setWebViewClient(new WebViewClient() {
                            @Override
                            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                Logger.printInfo(() -> "Url getCookiesFromWebView " + url);

                                if (url.contains("open.spotify.com")) {
                                    CookieManager cookieManager = CookieManager.getInstance();
                                    cookiesAtomicReference.set(cookieManager.getCookie("open.spotify.com"));
                                    dialog.dismiss();
                                    latch.countDown();
                                }
                            }
                        });

                        Logger.printInfo(() -> "loading url");
                        CookieManager.getInstance().setCookie("https://open.spotify.com", "");
                        webView.loadUrl("https://accounts.spotify.com/en/login?continue=https%3A%2F%2Fopen.spotify.com");
                        Logger.printInfo(() -> "setting view");
                        dialog.setView(webView);
                        Logger.printInfo(() -> "Showing dialog");
                        Utils.showDialog((Activity) context, dialog, false, null);
                    } catch (Throwable e) {
                        Logger.printInfo(() -> "djjjjjjj" + e);
                    }
                });

                try {
                    Logger.printInfo(() -> "waiting for latch");
                    latch.await();
                } catch (InterruptedException ignored) {
                    Logger.printException(() -> "latch error", ignored);
                }
            } catch (Exception ex) {
                Logger.printException(() -> "dsfsd", ex);
            }

            Logger.printInfo(() -> "waiting for cookies");
            String cookiesStr = cookiesAtomicReference.get();
            Logger.printInfo(() -> "Cookies " + cookiesStr);
            cookies = cookiesStr;
        }).start();
    }

    public static void openLoginWebView(LayoutInflater layoutInflater) {
        Context context = layoutInflater.getContext();
        getCookiesFromWebView(context);
    }

    /* public static void setLoginWebView(View inflatedView) {
        inflatedView.
    } */

    public static void startWebServer() {
        try {
            WebServer webServer = new WebServer(4345);
            webServer.start();
            Logger.printInfo(() -> "NanoHTTPD server running on http://127.0.0.1:" + 4345);
        } catch (Exception ex) {
            Logger.printException(() -> "startWebServer",  ex);
        }
    }

    public static void setClientToken(String newClientToken) {
        clientToken = newClientToken;
        Logger.printInfo(() -> "Client token set to " + newClientToken);
    }
}
