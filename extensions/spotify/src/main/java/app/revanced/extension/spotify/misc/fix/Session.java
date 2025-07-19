package app.revanced.extension.spotify.misc.fix;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

class Session {
    /**
     * Username of the account. Null if this session does not have an authenticated user.
     */
    @Nullable
    final String username;
    /**
     * Access token for this session.
     */
    final String accessToken;
    /**
     * Session expiration timestamp in milliseconds.
     */
    final Long expirationTime;
    /**
     * Authentication cookies for this session.
     */
    final String cookies;

    /**
     * Session that represents a failed attempt to renew the session.
     */
    static final Session FAILED_TO_RENEW_SESSION = new Session("", "", "");

    /**
     * @param username    Username of the account. Empty if this session does not have an authenticated user.
     * @param accessToken Access token for this session.
     * @param cookies     Authentication cookies for this session.
     */
    Session(@Nullable String username, String accessToken, String cookies) {
        this(username, accessToken, System.currentTimeMillis() + 60 * 60 * 1000, cookies);
    }

    private Session(@Nullable String username, String accessToken, long expirationTime, String cookies) {
        this.username = username;
        this.accessToken = accessToken;
        this.expirationTime = expirationTime;
        this.cookies = cookies;
    }

    /**
     * @return The number of milliseconds until the access token expires.
     */
    long accessTokenExpiresInMillis() {
        long currentTime = System.currentTimeMillis();
        return expirationTime - currentTime;
    }

    /**
     * @return The number of seconds until the access token expires.
     */
    int accessTokenExpiresInSeconds() {
        return (int) accessTokenExpiresInMillis() / 1000;
    }

    /**
     * @return True if the access token has expired, false otherwise.
     */
    boolean accessTokenExpired() {
        return accessTokenExpiresInMillis() <= 0;
    }

    void save() {
        Logger.printInfo(() -> "Saving session for username: " + username);

        SharedPreferences.Editor editor = Utils.getContext().getSharedPreferences("revanced", MODE_PRIVATE).edit();

        String json;
        try {
            json = new JSONObject()
                    .put("accessToken", accessToken)
                    .put("expirationTime", expirationTime)
                    .put("cookies", cookies).toString();
        } catch (JSONException ex) {
            Logger.printException(() -> "Failed to convert session to stored credential", ex);
            return;
        }

        editor.putString("session_" + username, json);
        editor.apply();
    }

    void delete() {
        Logger.printInfo(() -> "Deleting saved session for username: " + username);
        SharedPreferences.Editor editor = Utils.getContext().getSharedPreferences("revanced", MODE_PRIVATE).edit();
        editor.remove("session_" + username);
        editor.apply();
    }

    @Nullable
    static Session read(String username) {
        Logger.printInfo(() -> "Reading saved session for username: " + username);

        SharedPreferences sharedPreferences = Utils.getContext().getSharedPreferences("revanced", MODE_PRIVATE);
        String savedJson = sharedPreferences.getString("session_" + username, null);
        if (savedJson == null) {
            Logger.printInfo(() -> "No session found in shared preferences");
            return null;
        }

        try {
            JSONObject json = new JSONObject(savedJson);
            String accessToken = json.getString("accessToken");
            long expirationTime = json.getLong("expirationTime");
            String cookies = json.getString("cookies");

            return new Session(username, accessToken, expirationTime, cookies);
        } catch (JSONException ex) {
            Logger.printException(() -> "Failed to read session from shared preferences", ex);
            return null;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "Session(" +
                "username=" + username +
                ", accessToken=" + accessToken +
                ", expirationTime=" + expirationTime +
                ", cookies=" + cookies +
                ')';
    }
}
