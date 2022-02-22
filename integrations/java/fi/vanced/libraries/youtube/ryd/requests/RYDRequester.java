package fi.vanced.libraries.youtube.ryd.requests;

import static fi.razerman.youtube.XGlobals.debug;
import static fi.vanced.libraries.youtube.player.VideoInformation.dislikeCount;
import static fi.vanced.libraries.youtube.ryd.ReturnYouTubeDislikes.TAG;
import static fi.vanced.utils.requests.Requester.parseJson;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import fi.vanced.libraries.youtube.ryd.Registration;
import fi.vanced.libraries.youtube.ryd.ReturnYouTubeDislikes;
import fi.vanced.libraries.youtube.ryd.Utils;
import fi.vanced.utils.requests.Requester;
import fi.vanced.utils.requests.Route;

public class RYDRequester {
    private static final String RYD_API_URL = "https://returnyoutubedislikeapi.com/";

    private RYDRequester() {}

    public static void fetchDislikes(String videoId) {
        try {
            if (debug) {
                Log.d(TAG, "Fetching dislikes for " + videoId);
            }
            HttpURLConnection connection = getConnectionFromRoute(RYDRoutes.GET_DISLIKES, videoId);
            connection.setConnectTimeout(5 * 1000);
            if (connection.getResponseCode() == 200) {
                JSONObject json = getJSONObject(connection);
                int dislikes = json.getInt("dislikes");
                dislikeCount = dislikes;
                if (debug) {
                    Log.d(TAG, "dislikes fetched - " + dislikeCount);
                }

                // Set the dislikes
                new Handler(Looper.getMainLooper()).post(() -> ReturnYouTubeDislikes.trySetDislikes(ReturnYouTubeDislikes.formatDislikes(dislikes)));
            }
            else if (debug) {
                Log.d(TAG, "dislikes fetch response was " + connection.getResponseCode());
            }
            connection.disconnect();
        }
        catch (Exception ex) {
            dislikeCount = null;
            Log.e(TAG, "Failed to fetch dislikes", ex);
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
                if (debug) {
                    Log.d(TAG, "Registration challenge - " + challenge + " with difficulty of " + difficulty);
                }

                // Solve the puzzle
                String solution = Utils.solvePuzzle(challenge, difficulty);
                if (debug) {
                    Log.d(TAG, "Registration confirmation solution is " + solution);
                }

                return confirmRegistration(userId, solution, registration);
            }
            else if (debug) {
                Log.d(TAG, "Registration response was " + connection.getResponseCode());
            }
            connection.disconnect();
        }
        catch (Exception ex){
            Log.e(TAG, "Failed to register userId", ex);
        }
        return null;
    }

    private static String confirmRegistration(String userId, String solution, Registration registration) {
        try {
            if (debug) {
                Log.d(TAG, "Trying to confirm registration for the following userId: " + userId + " with solution: " + solution);
            }

            HttpURLConnection connection = getConnectionFromRoute(RYDRoutes.CONFIRM_REGISTRATION, userId);
            applyCommonRequestSettings(connection);

            String jsonInputString = "{\"solution\": \"" + solution + "\"}";
            try(OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            if (connection.getResponseCode() == 200) {
                String result = parseJson(connection);
                if (debug) {
                    Log.d(TAG, "Registration confirmation result was " + result);
                }

                if (result.equalsIgnoreCase("true")) {
                    registration.saveUserId(userId);
                    if (debug) {
                        Log.d(TAG, "Registration was successful for user " + userId);
                    }

                    return userId;
                }
            }
            else if (debug) {
                Log.d(TAG, "Registration confirmation response was " + connection.getResponseCode());
            }
            connection.disconnect();
        }
        catch (Exception ex) {
            Log.e(TAG, "Failed to confirm registration", ex);
        }

        return null;
    }

    public static boolean sendVote(String videoId, String userId, int vote) {
        try {
            HttpURLConnection connection = getConnectionFromRoute(RYDRoutes.SEND_VOTE);
            applyCommonRequestSettings(connection);

            String voteJsonString = "{\"userId\": \"" + userId + "\", \"videoId\": \"" + videoId + "\", \"value\": \"" + vote + "\"}";
            try(OutputStream os = connection.getOutputStream()) {
                byte[] input = voteJsonString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            if (connection.getResponseCode() == 200) {
                JSONObject json = getJSONObject(connection);
                String challenge = json.getString("challenge");
                int difficulty = json.getInt("difficulty");
                if (debug) {
                    Log.d(TAG, "Vote challenge - " + challenge + " with difficulty of " + difficulty);
                }

                // Solve the puzzle
                String solution = Utils.solvePuzzle(challenge, difficulty);
                if (debug) {
                    Log.d(TAG, "Vote confirmation solution is " + solution);
                }

                // Confirm vote
                return confirmVote(videoId, userId, solution);
            }
            else if (debug) {
                Log.d(TAG, "Vote response was " + connection.getResponseCode());
            }
            connection.disconnect();
        }
        catch (Exception ex) {
            Log.e(TAG, "Failed to send vote", ex);
        }
        return false;
    }

    private static boolean confirmVote(String videoId, String userId, String solution) {
        try {
            HttpURLConnection connection = getConnectionFromRoute(RYDRoutes.CONFIRM_VOTE);
            applyCommonRequestSettings(connection);

            String jsonInputString = "{\"userId\": \"" + userId + "\", \"videoId\": \"" + videoId + "\", \"solution\": \"" + solution + "\"}";
            try(OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            if (connection.getResponseCode() == 200) {
                String result = parseJson(connection);
                if (debug) {
                    Log.d(TAG, "Vote confirmation result was " + result);
                }

                if (result.equalsIgnoreCase("true")) {
                    if (debug) {
                        Log.d(TAG, "Vote was successful for user " + userId);
                    }

                    return true;
                }
            }
            else if (debug) {
                Log.d(TAG, "Vote confirmation response was " + connection.getResponseCode());
            }
            connection.disconnect();
        }
        catch (Exception ex) {
            Log.e(TAG, "Failed to confirm vote", ex);
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