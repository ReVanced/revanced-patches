package app.revanced.integrations.returnyoutubedislike.requests;

import static app.revanced.integrations.requests.Requester.parseJson;


import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import app.revanced.integrations.returnyoutubedislike.ReturnYouTubeDislike;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.returnyoutubedislike.Registration;
import app.revanced.integrations.requests.Requester;
import app.revanced.integrations.requests.Route;

public class ReturnYouTubeDislikeApi {
    private static final String RYD_API_URL = "https://returnyoutubedislikeapi.com/";

    private ReturnYouTubeDislikeApi() {
    }

    public static void fetchDislikes(String videoId) {
        try {
            LogHelper.debug(ReturnYouTubeDislikeApi.class, "Fetching dislikes for " + videoId);
            HttpURLConnection connection = getConnectionFromRoute(ReturnYouTubeDislikeRoutes.GET_DISLIKES, videoId);
            connection.setConnectTimeout(1000);
            if (connection.getResponseCode() == 200) {
                JSONObject json = getJSONObject(connection);
                ReturnYouTubeDislike.dislikeCount = json.getInt("dislikes");
                LogHelper.debug(ReturnYouTubeDislikeApi.class, "dislikes fetched - " + ReturnYouTubeDislike.dislikeCount);
            } else {
                LogHelper.debug(ReturnYouTubeDislikeApi.class, "dislikes fetch response was " + connection.getResponseCode());
            }
            connection.disconnect();
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislikeApi.class, "Failed to fetch dislikes", ex);
        }
    }

    public static String register(String userId, Registration registration) {
        try {
            HttpURLConnection connection = getConnectionFromRoute(ReturnYouTubeDislikeRoutes.GET_REGISTRATION, userId);
            connection.setConnectTimeout(5 * 1000);
            if (connection.getResponseCode() == 200) {
                JSONObject json = getJSONObject(connection);
                String challenge = json.getString("challenge");
                int difficulty = json.getInt("difficulty");
                LogHelper.debug(ReturnYouTubeDislikeApi.class, "Registration challenge - " + challenge + " with difficulty of " + difficulty);

                // Solve the puzzle
                String solution = Registration.solvePuzzle(challenge, difficulty);
                LogHelper.debug(ReturnYouTubeDislikeApi.class, "Registration confirmation solution is " + solution);

                return confirmRegistration(userId, solution, registration);
            } else {
                LogHelper.debug(ReturnYouTubeDislikeApi.class, "Registration response was " + connection.getResponseCode());
            }
            connection.disconnect();
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislikeApi.class, "Failed to register userId", ex);
        }
        return null;
    }

    private static String confirmRegistration(String userId, String solution, Registration registration) {
        try {
            LogHelper.debug(ReturnYouTubeDislikeApi.class, "Trying to confirm registration for the following userId: " + userId + " with solution: " + solution);

            HttpURLConnection connection = getConnectionFromRoute(ReturnYouTubeDislikeRoutes.CONFIRM_REGISTRATION, userId);
            applyCommonRequestSettings(connection);

            String jsonInputString = "{\"solution\": \"" + solution + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            if (connection.getResponseCode() == 200) {
                String result = parseJson(connection);
                LogHelper.debug(ReturnYouTubeDislikeApi.class, "Registration confirmation result was " + result);

                if (result.equalsIgnoreCase("true")) {
                    registration.saveUserId(userId);
                    LogHelper.debug(ReturnYouTubeDislikeApi.class, "Registration was successful for user " + userId);

                    return userId;
                }
            } else {
                LogHelper.debug(ReturnYouTubeDislikeApi.class, "Registration confirmation response was " + connection.getResponseCode());
            }
            connection.disconnect();
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislikeApi.class, "Failed to confirm registration", ex);
        }

        return null;
    }

    public static boolean sendVote(String videoId, String userId, int vote) {
        try {
            HttpURLConnection connection = getConnectionFromRoute(ReturnYouTubeDislikeRoutes.SEND_VOTE);
            applyCommonRequestSettings(connection);

            String voteJsonString = "{\"userId\": \"" + userId + "\", \"videoId\": \"" + videoId + "\", \"value\": \"" + vote + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = voteJsonString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            if (connection.getResponseCode() == 200) {
                JSONObject json = getJSONObject(connection);
                String challenge = json.getString("challenge");
                int difficulty = json.getInt("difficulty");
                LogHelper.debug(ReturnYouTubeDislikeApi.class, "Vote challenge - " + challenge + " with difficulty of " + difficulty);

                // Solve the puzzle
                String solution = Registration.solvePuzzle(challenge, difficulty);
                LogHelper.debug(ReturnYouTubeDislikeApi.class, "Vote confirmation solution is " + solution);

                // Confirm vote
                return confirmVote(videoId, userId, solution);
            } else {
                LogHelper.debug(ReturnYouTubeDislikeApi.class, "Vote response was " + connection.getResponseCode());
            }
            connection.disconnect();
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislikeApi.class, "Failed to send vote", ex);
        }
        return false;
    }

    private static boolean confirmVote(String videoId, String userId, String solution) {
        try {
            HttpURLConnection connection = getConnectionFromRoute(ReturnYouTubeDislikeRoutes.CONFIRM_VOTE);
            applyCommonRequestSettings(connection);

            String jsonInputString = "{\"userId\": \"" + userId + "\", \"videoId\": \"" + videoId + "\", \"solution\": \"" + solution + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            if (connection.getResponseCode() == 200) {
                String result = parseJson(connection);
                LogHelper.debug(ReturnYouTubeDislikeApi.class, "Vote confirmation result was " + result);


                if (result.equalsIgnoreCase("true")) {
                    LogHelper.debug(ReturnYouTubeDislikeApi.class, "Vote was successful for user " + userId);

                    return true;
                }
            } else {
                LogHelper.debug(ReturnYouTubeDislikeApi.class, "Vote confirmation response was " + connection.getResponseCode());
            }
            connection.disconnect();
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislikeApi.class, "Failed to confirm vote", ex);
        }
        return false;
    }

    // utils

    private static void applyCommonRequestSettings(HttpURLConnection connection) throws Exception {
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(5 * 1000);
    }

    // helpers

    private static HttpURLConnection getConnectionFromRoute(Route route, String... params) throws IOException {
        return Requester.getConnectionFromRoute(RYD_API_URL, route, params);
    }

    private static JSONObject getJSONObject(HttpURLConnection connection) throws Exception {
        return Requester.getJSONObject(connection);
    }
}
