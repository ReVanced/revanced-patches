package fi.vanced.libraries.youtube.ryd;

import static fi.razerman.youtube.XGlobals.debug;
import static fi.vanced.libraries.youtube.ryd.RYDSettings.PREFERENCES_KEY_USERID;
import static fi.vanced.libraries.youtube.ryd.RYDSettings.PREFERENCES_NAME;
import static fi.vanced.utils.VancedUtils.getPreferences;
import static fi.vanced.utils.VancedUtils.parseJson;
import static fi.vanced.utils.VancedUtils.randomString;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Registration {
    private static final String TAG = "VI - RYD - Registration";

    private String userId;
    private Context context;

    public Registration(Context context) {
        this.context = context;
    }

    public String getUserId() {
        return userId != null ? userId : fetchUserId();
    }

    private String fetchUserId() {
        try {
            if (this.context == null) throw new Exception("Unable to fetch userId because context was null");

            SharedPreferences preferences = getPreferences(context, PREFERENCES_NAME);
            this.userId = preferences.getString(PREFERENCES_KEY_USERID, null);

            if (this.userId == null) {
                this.userId = register();
            }
        }
        catch (Exception ex) {
            Log.e(TAG, "Unable to fetch the userId from shared preferences", ex);
        }

        return this.userId;
    }

    private void saveUserId(String userId) {
        try {
            if (this.context == null) throw new Exception("Unable to save userId because context was null");

            SharedPreferences preferences = getPreferences(context, PREFERENCES_NAME);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PREFERENCES_KEY_USERID, userId).apply();
        }
        catch (Exception ex) {
            Log.e(TAG, "Unable to save the userId in shared preferences", ex);
        }
    }

    private String register() {
        try {
            // Generate a new userId
            String userId = randomString(36);
            if (debug) {
                Log.d(TAG, "Trying to register the following userId: " + userId);
            }

            // Get the registration challenge
            HttpURLConnection connection = (HttpURLConnection) new URL(ReturnYouTubeDislikes.RYD_API_URL + "/puzzle/registration?userId=" + userId).openConnection();
            connection.setRequestProperty("User-agent", System.getProperty("http.agent") + ";vanced");
            connection.setConnectTimeout(5 * 1000);
            if (connection.getResponseCode() == 200) {
                JSONObject json = new JSONObject(parseJson(connection));
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

                return confirmRegistration(userId, solution);
            }
            else if (debug) {
                Log.d(TAG, "Registration response was " + connection.getResponseCode());
            }
        }
        catch (Exception ex) {
            Log.e(TAG, "Failed to register userId", ex);
        }

        return null;
    }

    public String confirmRegistration(String userId, String solution) {
        try {
            if (debug) {
                Log.d(TAG, "Trying to confirm registration for the following userId: " + userId + " with solution: " + solution);
            }

            // Confirm registration
            HttpURLConnection confirmationCon = (HttpURLConnection) new URL(ReturnYouTubeDislikes.RYD_API_URL + "/puzzle/registration?userId=" + userId).openConnection();
            confirmationCon.setRequestProperty("User-agent", System.getProperty("http.agent") + ";vanced");
            confirmationCon.setRequestMethod("POST");
            confirmationCon.setRequestProperty("Content-Type", "application/json");
            confirmationCon.setRequestProperty("Accept", "application/json");
            confirmationCon.setDoOutput(true);
            confirmationCon.setConnectTimeout(5 * 1000);

            String jsonInputString = "{\"solution\": \"" + solution + "\"}";
            try(OutputStream os = confirmationCon.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            if (confirmationCon.getResponseCode() == 200) {
                String result = parseJson(confirmationCon);
                if (debug) {
                    Log.d(TAG, "Registration confirmation result was " + result);
                }

                if (result.equalsIgnoreCase("true")) {
                    saveUserId(userId);
                    if (debug) {
                        Log.d(TAG, "Registration was successful for user " + userId);
                    }

                    return userId;
                }
            }
            else if (debug) {
                Log.d(TAG, "Registration confirmation response was " + confirmationCon.getResponseCode());
            }
        }
        catch (Exception ex) {
            Log.e(TAG, "Failed to confirm registration", ex);
        }

        return null;
    }
}
