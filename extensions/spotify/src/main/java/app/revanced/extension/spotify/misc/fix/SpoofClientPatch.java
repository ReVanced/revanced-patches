package app.revanced.extension.spotify.misc.fix;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.*;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public final class SpoofClientPatch {

    public static String transferSession(String accessToken) {
        try {
            Log.d("revanced", accessToken);

            String ottTokenResponse = getOttTokenResponse(accessToken, "AADrDtwPkc7J1NUeZTRTKFhQh569h38IIbQ6TQbVaIAxWmo+bSN7XBMrwil0EfFolufoDsNq9bXa+w4mKHAYBJNPzbj9oTOUokIl+yx7T+aTLs44V6d7Z8iAF+Loif+VOeIn/Qnsqax5zumW3mAXwW0W6jGSWRyf3LFatT07KILjzH06QHaOUzBK3nnETDP87+/C2xNWej0pmNTs20yF3YjbrzHuEbiGksD2mT7bi+KVpubeQKzJV/xTUfpNi7OaxY9GRLc/nCSkaW1r3P8fYANej+U5t1kLawecltZzBXB8OiUNzoTIn8FNqp2wp1ypBeORpydHP5e3hmIjLFxYnEYcHaG1qmef0X6GG4KVencg6SHiMDeX6gOXXZ/4GVn9Fsd4iYNKhMNzA4ZOCu0Fh4uSCwDIjhg=");
            String ottToken = new JSONObject(ottTokenResponse).getString("token");

            Log.d("revanced", "ottToken " + ottToken);
            String webBearerToken = getWebBearerTokenResponse(ottToken).replace("Bearer ", "");
            Log.d("revanced", "webAccessToken " + webBearerToken);

            return webBearerToken;
        } catch(Exception ex) {
            Logger.printException(() -> "transferSession failure", ex);
        }
        return accessToken;
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

        CountDownLatch latch = new CountDownLatch(1);

        Utils.runOnMainThread(() -> {
            WebView webView = new WebView(Utils.getContext());
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36 Edg/137.0.0.0");

            webView.setWebViewClient(new WebViewClient() {
                private boolean ottVerified;
                private boolean patchedFetch;

                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    // Obtain necessary cookies for the session transfer.
                    if (!ottVerified && request.getUrl().toString().contains("/api/login/ott/verify")) {
                        Log.d("revanced", "ottVerified");
                        ottVerified = true;

                        Utils.runOnMainThread(() -> {
                            view.loadUrl("https://open.spotify.com");
                        });
                    }

                    return super.shouldInterceptRequest(view, request);
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    Log.d("revanced", url);
                    // Hook into fetch requests to capture the Authorization header.
                    if (!patchedFetch && url.contains("open.spotify.com")) {
                        String jsHook = "(function() {" +
                                "   const originalFetch = window.fetch;" +
                                "   window.fetch = (input, init) => {" +
                                "       const request = typeof input === 'string' ? new Request(input, init) : input;" +
                                "       const header = request.headers?.get?.('Authorization') || (init?.headers?.Authorization);" +
                                "       if (header) androidBridge.receiveToken(header);" +
                                "       return originalFetch(input, init);" +
                                "   };" +
                                "})();";

                        Log.d("revanced", "executing js");
                        view.evaluateJavascript(jsHook, null);
                        patchedFetch = true;
                    }
                }
            });

            webView.addJavascriptInterface(new Object() {
                @JavascriptInterface
                public void receiveToken(String token) {
                    Log.d("revanced", "Token received via JS: " + token);
                    webBearerTokenResponse.set(token);
                    latch.countDown();
                }
            }, "androidBridge");

            String startUrl = "https://accounts.spotify.com/en/login/ott/v2#token=" + ottToken;
            Log.d("revanced", "loading url " + startUrl);
            webView.loadUrl(startUrl);
        });

        try {
            latch.await();
        } catch (InterruptedException ignored) {
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
