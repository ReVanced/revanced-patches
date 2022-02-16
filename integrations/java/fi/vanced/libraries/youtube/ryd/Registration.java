package fi.vanced.libraries.youtube.ryd;

import static fi.razerman.youtube.XGlobals.debug;
import static fi.vanced.libraries.youtube.ryd.RYDSettings.PREFERENCES_KEY_USERID;
import static fi.vanced.libraries.youtube.ryd.RYDSettings.PREFERENCES_NAME;
import static fi.vanced.utils.VancedUtils.getPreferences;
import static fi.vanced.utils.VancedUtils.randomString;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import fi.vanced.libraries.youtube.ryd.requests.RYDRequester;

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

    public void saveUserId(String userId) {
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
        String userId = randomString(36);
        if (debug) {
            Log.d(TAG, "Trying to register the following userId: " + userId);
        }
        return RYDRequester.register(userId, this);
    }
}
