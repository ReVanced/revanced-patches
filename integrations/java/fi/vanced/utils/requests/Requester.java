package fi.vanced.utils.requests;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import fi.vanced.libraries.youtube.whitelisting.requests.WhitelistRoutes;

public class Requester {
    private Requester() {}

    public static HttpURLConnection getConnectionFromRoute(String apiUrl, Route route, String... params) throws IOException {
        String url = apiUrl + route.compile(params).getCompiledRoute();
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(route.getMethod().name());
        if (route != WhitelistRoutes.GET_CHANNEL_DETAILS) {
            connection.setRequestProperty("User-agent", System.getProperty("http.agent") + ";vanced");
        }
        return connection;
    }

    public static String parseJson(HttpURLConnection connection) throws IOException {
        return parseJson(connection.getInputStream(), false);
    }

    public static String parseJson(InputStream inputStream, boolean isError) throws IOException {
        StringBuilder jsonBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
            if (isError)
                jsonBuilder.append("\n");
        }
        inputStream.close();
        return jsonBuilder.toString();
    }

    public static String parseErrorJson(HttpURLConnection connection) throws IOException {
        return parseJson(connection.getErrorStream(), true);
    }

    public static JSONObject getJSONObject(HttpURLConnection connection) throws Exception {
        return new JSONObject(parseJsonAndDisconnect(connection));
    }

    public static JSONArray getJSONArray(HttpURLConnection connection) throws Exception {
        return new JSONArray(parseJsonAndDisconnect(connection));
    }

    private static String parseJsonAndDisconnect(HttpURLConnection connection) throws IOException {
        String json = parseJson(connection);
        connection.disconnect();
        return json;
    }
}