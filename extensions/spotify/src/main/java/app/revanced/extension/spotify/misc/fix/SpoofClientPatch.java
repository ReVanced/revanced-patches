package app.revanced.extension.spotify.misc.fix;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.*;
import app.revanced.extension.shared.Utils;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class SpoofClientPatch {
    public static String transferSession(String accessToken, String clientToken) throws Exception {
        var ottToken = new JSONObject(getOttTokenResponse(accessToken, clientToken)).getString("token");

        var webBearerTokenResponse = new JSONObject(getWebBearerTokenResponse(ottToken));
        return webBearerTokenResponse.getString("access_token");
    }

    private static String getOttTokenResponse(String accessToken, String clientToken) throws Exception {
        URL url = new URL("https://gew4-spclient.spotify.com/sessiontransfer/v1/token");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setRequestProperty("client-token", clientToken);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            os.write("{\"url\": \"https://www.spotify.com/account/profile-mobile\"}".getBytes());
            os.flush();
        }
        return readConnectionResponse(connection);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private static String getWebBearerTokenResponse(String ottToken) {
        AtomicReference<String> webBearerTokenResponse = new AtomicReference<>();

        var latch = new CountDownLatch(1);

        WebView webView = new WebView(Utils.getContext());
        var settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36 Edg/137.0.0.0");

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        webView.setWebViewClient(new WebViewClient() {
            private boolean ottVerified;
            private boolean tokenApiIntercepted;

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                if (!ottVerified && url.contains("/api/login/ott/verify")) {
                    ottVerified = true;
                    Log.d("revanced", "Intercepted /api/login/ott/verify");
                    new Handler(Looper.getMainLooper()).post(() -> webView.loadUrl("https://open.spotify.com"));
                } else if (!tokenApiIntercepted && url.contains("/api/token")) {
                    tokenApiIntercepted = true;
                    Log.d("revanced", "Intercepted /api/token");

                    new Thread(() -> {
                        try {
                            URL tokenUrl = new URL(url);
                            HttpURLConnection connection = (HttpURLConnection) tokenUrl.openConnection();
                            connection.setRequestMethod("GET");

                            Map<String, String> requestHeaders = request.getRequestHeaders();
                            for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                                Log.d("revanced", "Request Header: " + entry.getKey() + ": " + entry.getValue());
                                connection.setRequestProperty(entry.getKey(), entry.getValue());
                            }
                            connection.setRequestProperty("User-Agent", webView.getSettings().getUserAgentString());
                            connection.setRequestProperty("Cookie", cookieManager.getCookie("https://open.spotify.com"));

                            connection.connect();

                            var response = readConnectionResponse(connection);
                            Log.d("revanced", "Token Response: " + response);

                            webBearerTokenResponse.set(response);
                            webView.stopLoading();
                        } catch (Exception e) {
                            Log.e("revanced", "Failed to fetch /api/token", e);
                        }

                        latch.countDown();
                    }).start();

                    return null;
                }

                return super.shouldInterceptRequest(view, request);
            }

        });

        String startUrl = "https://accounts.spotify.com/en/login/ott/v2#token=" + ottToken;
        webView.loadUrl(startUrl);

        try {
            latch.await();
        } catch (InterruptedException e) {
            return null;
        }

        return webBearerTokenResponse.get();
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
