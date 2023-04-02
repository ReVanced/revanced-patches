package app.revanced.integrations.requests;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Requester {
    private Requester() {
    }

    public static HttpURLConnection getConnectionFromRoute(String apiUrl, Route route, String... params) throws IOException {
        String url = apiUrl + route.compile(params).getCompiledRoute();
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(route.getMethod().name());
        connection.setRequestProperty("User-agent", System.getProperty("http.agent") + ";revanced");

        return connection;
    }

    /**
     * Parse the {@link HttpURLConnection}, and closes the underlying InputStream.
     */
    public static String parseJson(HttpURLConnection connection) throws IOException {
        return parseInputStreamAndClose(connection.getInputStream(), true);
    }

    /**
     * Parse the {@link HttpURLConnection}, close the underlying InputStream, and disconnect.
     *
     * <b>Should only be used if other requests to the server are unlikely in the near future</b>
     *
     * @see #parseJson(HttpURLConnection)
     */
    public static String parseJsonAndDisconnect(HttpURLConnection connection) throws IOException {
        String result = parseJson(connection);
        connection.disconnect();
        return result;
    }

    /**
     * Parse the {@link HttpURLConnection}, and closes the underlying InputStream.
     *
     * @param stripNewLineCharacters if newline (\n) characters should be stripped from the InputStream
     */
    public static String parseInputStreamAndClose(InputStream inputStream, boolean stripNewLineCharacters) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
                if (!stripNewLineCharacters)
                    jsonBuilder.append("\n");
            }
            return jsonBuilder.toString();
        }
    }

    /**
     * Parse the {@link HttpURLConnection}, and closes the underlying InputStream.
     */
    public static String parseErrorJson(HttpURLConnection connection) throws IOException {
        return parseInputStreamAndClose(connection.getErrorStream(), false);
    }

    /**
     * Parse the {@link HttpURLConnection}, close the underlying InputStream, and disconnect.
     *
     * <b>Should only be used if other requests to the server are unlikely in the near future</b>
     *
     * @see #parseErrorJson(HttpURLConnection)
     */
    public static String parseErrorJsonAndDisconnect(HttpURLConnection connection) throws IOException {
        String result = parseErrorJson(connection);
        connection.disconnect();
        return result;
    }

    /**
     * Parse the {@link HttpURLConnection}, and closes the underlying InputStream.
     */
    public static JSONObject parseJSONObject(HttpURLConnection connection) throws JSONException, IOException {
        return new JSONObject(parseJson(connection));
    }

    /**
     * Parse the {@link HttpURLConnection}, close the underlying InputStream, and disconnect.
     *
     * <b>Should only be used if other requests to the server are unlikely in the near future</b>
     *
     * @see #parseJSONObject(HttpURLConnection)
     */
    public static JSONObject parseJSONObjectAndDisconnect(HttpURLConnection connection) throws JSONException, IOException {
        JSONObject object = parseJSONObject(connection);
        connection.disconnect();
        return object;
    }

    /**
     * Parse the {@link HttpURLConnection}, and closes the underlying InputStream.
     */
    public static JSONArray parseJSONArray(HttpURLConnection connection) throws JSONException, IOException  {
        return new JSONArray(parseJson(connection));
    }

    /**
     * Parse the {@link HttpURLConnection}, close the underlying InputStream, and disconnect.
     *
     * <b>Should only be used if other requests to the server are unlikely in the near future</b>
     *
     * @see #parseJSONArray(HttpURLConnection)
     */
    public static JSONArray parseJSONArrayAndDisconnect(HttpURLConnection connection) throws JSONException, IOException  {
        JSONArray array = parseJSONArray(connection);
        connection.disconnect();
        return array;
    }

}