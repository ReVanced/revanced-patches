package fi.vanced.libraries.youtube.ryd;

import static fi.razerman.youtube.XGlobals.debug;
import static fi.vanced.utils.VancedUtils.parseJson;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Voting {
    private static final String TAG = "VI - RYD - Voting";

    private Registration registration;
    private Context context;

    public Voting(Context context, Registration registration) {
        this.context = context;
        this.registration = registration;
    }

    public boolean sendVote(String videoId, int vote) {
        try {
            String userId = registration.getUserId();
            if (debug) {
                Log.d(TAG, "Trying to vote the following video: " + videoId + " with vote " + vote + " and userId: " + userId);
            }

            // Send the vote
            HttpURLConnection connection = (HttpURLConnection) new URL(ReturnYouTubeDislikes.RYD_API_URL + "/interact/vote").openConnection();
            connection.setRequestProperty("User-agent", System.getProperty("http.agent") + ";vanced");
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5 * 1000);
            String voteJsonString = "{\"userId\": \"" + userId + "\", \"videoId\": \"" + videoId + "\", \"value\": \"" + vote + "\"}";
            try(OutputStream os = connection.getOutputStream()) {
                byte[] input = voteJsonString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            if (connection.getResponseCode() == 200) {
                JSONObject json = new JSONObject(parseJson(connection));
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
                return confirmVote(userId, videoId, solution);
            }
            else if (debug) {
                Log.d(TAG, "Vote response was " + connection.getResponseCode());
            }
        }
        catch (Exception ex) {
            Log.e(TAG, "Failed to send vote", ex);
        }

        return false;
    }

    public boolean confirmVote(String userId, String videoId, String solution) {
        try {
            if (debug) {
                Log.d(TAG, "Trying to confirm vote for video: " + videoId + " with solution " + solution + " and userId: " + userId);
            }

            // Confirm vote
            HttpURLConnection confirmationCon = (HttpURLConnection) new URL(ReturnYouTubeDislikes.RYD_API_URL + "/interact/confirmVote").openConnection();
            confirmationCon.setRequestProperty("User-agent", System.getProperty("http.agent") + ";vanced");
            confirmationCon.setRequestMethod("POST");
            confirmationCon.setRequestProperty("Content-Type", "application/json");
            confirmationCon.setRequestProperty("Accept", "application/json");
            confirmationCon.setDoOutput(true);
            confirmationCon.setConnectTimeout(5 * 1000);

            String jsonInputString = "{\"userId\": \"" + userId + "\", \"videoId\": \"" + videoId + "\", \"solution\": \"" + solution + "\"}";
            try(OutputStream os = confirmationCon.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            if (confirmationCon.getResponseCode() == 200) {
                String result = parseJson(confirmationCon);
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
                Log.d(TAG, "Vote confirmation response was " + confirmationCon.getResponseCode());
            }
        }
        catch (Exception ex) {
            Log.e(TAG, "Failed to send vote", ex);
        }

        return false;
    }
}
