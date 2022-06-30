package app.revanced.integrations.ryd;


import android.content.Context;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.SecureRandom;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.ryd.requests.RYDRequester;
import app.revanced.integrations.utils.SharedPrefHelper;

public class Registration {
    private String userId;
    private Context context;

    public Registration(Context context) {
        this.context = context;
    }

    // https://stackoverflow.com/a/157202
    private final String AB = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private SecureRandom rnd = new SecureRandom();

    public String getUserId() {
        return userId != null ? userId : fetchUserId();
    }

    public void saveUserId(String userId) {
        try {
            if (this.context == null)
                throw new Exception("Unable to save userId because context was null");
            SharedPrefHelper.saveString(context, SharedPrefHelper.SharedPrefNames.RYD, RYDSettings.PREFERENCES_KEY_USERID, userId);
        } catch (Exception ex) {
            LogHelper.printException(Registration.class, "Unable to save the userId in shared preferences", ex);
        }
    }

    public static String solvePuzzle(String challenge, int difficulty) {
        byte[] decodedChallenge = Base64.decode(challenge, Base64.NO_WRAP);

        byte[] buffer = new byte[20];
        for (int i = 4; i < 20; i++) {
            buffer[i] = decodedChallenge[i - 4];
        }

        try {
            int maxCount = (int) (Math.pow(2, difficulty + 1) * 5);
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            for (int i = 0; i < maxCount; i++) {
                buffer[0] = (byte) i;
                buffer[1] = (byte) (i >> 8);
                buffer[2] = (byte) (i >> 16);
                buffer[3] = (byte) (i >> 24);
                byte[] messageDigest = md.digest(buffer);

                if (countLeadingZeroes(messageDigest) >= difficulty) {
                    String encode = Base64.encodeToString(new byte[]{buffer[0], buffer[1], buffer[2], buffer[3]}, Base64.NO_WRAP);
                    return encode;
                }
            }
        } catch (Exception ex) {
            LogHelper.printException(Registration.class, "Failed to solve puzzle", ex);
        }

        return null;
    }

    private String register() {
        String userId = randomString(36);
        LogHelper.debug(Registration.class, "Trying to register the following userId: " + userId);
        return RYDRequester.register(userId, this);
    }

    private String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    private String fetchUserId() {
        try {
            if (this.context == null)
                throw new Exception("Unable to fetch userId because context was null");

            this.userId = SharedPrefHelper.getString(context, SharedPrefHelper.SharedPrefNames.RYD, RYDSettings.PREFERENCES_KEY_USERID, null);

            if (this.userId == null) {
                this.userId = register();
            }
        } catch (Exception ex) {
            LogHelper.printException(Registration.class, "Unable to fetch the userId from shared preferences", ex);
        }

        return this.userId;
    }

    private static int countLeadingZeroes(byte[] uInt8View) {
        int zeroes = 0;
        int value = 0;
        for (int i = 0; i < uInt8View.length; i++) {
            value = uInt8View[i] & 0xFF;
            if (value == 0) {
                zeroes += 8;
            } else {
                int count = 1;
                if (value >>> 4 == 0) {
                    count += 4;
                    value <<= 4;
                }
                if (value >>> 6 == 0) {
                    count += 2;
                    value <<= 2;
                }
                zeroes += count - (value >>> 7);
                break;
            }
        }
        return zeroes;
    }
}
