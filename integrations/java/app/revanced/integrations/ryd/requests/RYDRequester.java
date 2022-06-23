package app.revanced.integrations.ryd.requests;

import static app.revanced.integrations.sponsorblock.player.VideoInformation.dislikeCount;
import static app.revanced.integrations.adremover.whitelist.requests.Requester.parseJson;

import android.os.Handler;
import android.os.Looper;


import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.ryd.Registration;
import app.revanced.integrations.ryd.ReturnYouTubeDislikes;
import app.revanced.integrations.ryd.Utils;
import app.revanced.integrations.adremover.whitelist.requests.Requester;
import app.revanced.integrations.adremover.whitelist.requests.Route;

public class RYDRequester {
    private static final String RYD_API_URL = "https://returnyoutubedislikeapi.com/";

    private RYDRequester() {
    }

    public static void fetchDislikes(String videoId) {
        try {
            LogHelper.debug("RYDRequester", "Fetching dislikes for " + videoId);
            HttpURLConnection connection = getConnectionFromRoute(RYDRoutes.GET_DISLIKES, videoId);
            connection.setConnectTimeout(5 * 1000);
            if (connection.getResponseCode() == 200) {
                JSONObject json = getJSONObject(connection);
                int dislikes = json.getInt("dislikes");
                dislikeCount = dislikes;
                LogHelper.debug("RYDRequester", "dislikes fetched - " + dislikeCount);


                // Set the dislikes
                new Handler(Looper.getMainLooper()).post(() -> ReturnYouTubeDislikes.trySetDislikes(ReturnYouTubeDislikes.formatDislikes(dislikes)));
            } else {
                LogHelper.debug("RYDRequester", "dislikes fetch response was " + connection.getResponseCode());
            }
            connection.disconnect();
        } catch (Exception ex) {
            dislikeCount = null;
            LogHelper.printException("RYDRequester", "Failed to fetch dislikes", ex);
        }
    }

    public static String register(String userId, Registration registration) {
        try {
            HttpURLConnection connection = getConnectionFromRoute(RYDRoutes.GET_REGISTRATION, userId);
            connection.setConnectTimeout(5 * 1000);
            if (connection.getResponseCode() == 200) {
                JSONObject json = getJSONObject(connection);
                String challenge = json.getString("challenge");
                int difficulty = json.getInt("difficulty");
                LogHelper.debug("RYDRequester", "Registration challenge - " + challenge + " with difficulty of " + difficulty);

                // Solve the puzzle
                String solution = Utils.solvePuzzle(challenge, difficulty);
                LogHelper.debug("RYDRequester", "Registration confirmation solution is " + solution);

                return confirmRegistration(userId, solution, registration);
            } else {
                LogHelper.debug("RYDRequester", "Registration response was " + connection.getResponseCode());
            }
            connection.disconnect();
        } catch (Exception ex) {
            LogHelper.printException("RYDRequester", "Failed to register userId", ex);
        }
        return null;
    }

    private static String confirmRegistration(String userId, String solution, Registration registration) {
        try {
            LogHelper.debug("RYDRequester", "Trying to confirm registration for the following userId: " + userId + " with solution: " + solution);

            HttpURLConnection connection = getConnectionFromRoute(RYDRoutes.CONFIRM_REGISTRATION, userId);
            applyCommonRequestSettings(connection);

            String jsonInputString = "{\"solution\": \"" + solution + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            if (connection.getResponseCode() == 200) {
                String result = parseJson(connection);
                LogHelper.debug("RYDRequester", "Registration confirmation result was " + result);

                if (result.equalsIgnoreCase("true")) {
                    registration.saveUserId(userId);
                    LogHelper.debug("RYDRequester", "Registration was successful for user " + userId);

                    return userId;
                }
            } else {
                LogHelper.debug("RYDRequester", "Registration confirmation response was " + connection.getResponseCode());
            }
            connection.disconnect();
        } catch (Exception ex) {
            LogHelper.printException("RYDRequester", "Failed to confirm registration", ex);
        }

        return null;
    }

    public static boolean sendVote(String videoId, String userId, int vote) {
        try {
            HttpURLConnection connection = getConnectionFromRoute(RYDRoutes.SEND_VOTE);
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
                LogHelper.debug("RYDRequester", "Vote challenge - " + challenge + " with difficulty of " + difficulty);

                // Solve the puzzle
                String solution = Utils.solvePuzzle(challenge, difficulty);
                LogHelper.debug("RYDRequester", "Vote confirmation solution is " + solution);

                // Confirm vote
                return confirmVote(videoId, userId, solution);
            } else {
                LogHelper.debug("RYDRequester", "Vote response was " + connection.getResponseCode());
            }
            connection.disconnect();
        } catch (Exception ex) {
            LogHelper.printException("RYDRequester", "Failed to send vote", ex);
        }
        return false;
    }

    private static boolean confirmVote(String videoId, String userId, String solution) {
        try {
            HttpURLConnection connection = getConnectionFromRoute(RYDRoutes.CONFIRM_VOTE);
            applyCommonRequestSettings(connection);

            String jsonInputString = "{\"userId\": \"" + userId + "\", \"videoId\": \"" + videoId + "\", \"solution\": \"" + solution + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            if (connection.getResponseCode() == 200) {
                String result = parseJson(connection);
                LogHelper.debug("RYDRequester", "Vote confirmation result was " + result);


                if (result.equalsIgnoreCase("true")) {
                    LogHelper.debug("RYDRequester", "Vote was successful for user " + userId);

                    return true;
                }
            } else {
                LogHelper.debug("RYDRequester", "Vote confirmation response was " + connection.getResponseCode());
            }
            connection.disconnect();
        } catch (Exception ex) {
            LogHelper.printException("RYDRequester", "Failed to confirm vote", ex);
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