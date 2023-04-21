package app.revanced.integrations.sponsorblock;

import static app.revanced.integrations.utils.StringRef.str;

import android.content.SharedPreferences;
import android.util.Patterns;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.settings.SharedPrefCategory;
import app.revanced.integrations.sponsorblock.objects.CategoryBehaviour;
import app.revanced.integrations.sponsorblock.objects.SegmentCategory;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class SponsorBlockSettings {

    public static void importSettings(@NonNull String json) {
        ReVancedUtils.verifyOnMainThread();
        try {
            JSONObject settingsJson = new JSONObject(json);
            JSONObject barTypesObject = settingsJson.getJSONObject("barTypes");
            JSONArray categorySelectionsArray = settingsJson.getJSONArray("categorySelections");

            for (SegmentCategory category : SegmentCategory.categoriesWithoutUnsubmitted()) {
                // clear existing behavior, as browser plugin exports no behavior for ignored categories
                category.behaviour = CategoryBehaviour.IGNORE;
                if (barTypesObject.has(category.key)) {
                    JSONObject categoryObject = barTypesObject.getJSONObject(category.key);
                    category.setColor(categoryObject.getString("color"));
                }
            }

            for (int i = 0; i < categorySelectionsArray.length(); i++) {
                JSONObject categorySelectionObject = categorySelectionsArray.getJSONObject(i);

                String categoryKey = categorySelectionObject.getString("name");
                SegmentCategory category = SegmentCategory.byCategoryKey(categoryKey);
                if (category == null) {
                    continue; // unsupported category, ignore
                }

                final int desktopKey = categorySelectionObject.getInt("option");
                CategoryBehaviour behaviour = CategoryBehaviour.byDesktopKey(desktopKey);
                if (behaviour == null) {
                    ReVancedUtils.showToastLong(categoryKey + " unknown behavior key: " + desktopKey);
                } else if (category == SegmentCategory.HIGHLIGHT && behaviour == CategoryBehaviour.SKIP_AUTOMATICALLY_ONCE) {
                    ReVancedUtils.showToastLong("Skip-once behavior not allowed for " + category.key);
                    category.behaviour = CategoryBehaviour.SKIP_AUTOMATICALLY; // use closest match
                } else {
                    category.behaviour = behaviour;
                }
            }
            SegmentCategory.updateEnabledCategories();

            SharedPreferences.Editor editor = SharedPrefCategory.SPONSOR_BLOCK.preferences.edit();
            for (SegmentCategory category : SegmentCategory.categoriesWithoutUnsubmitted()) {
                category.save(editor);
            }
            editor.apply();

            String userID = settingsJson.getString("userID");
            if (!isValidSBUserId(userID)) {
                throw new IllegalArgumentException("userId is blank");
            }
            SettingsEnum.SB_UUID.saveValue(userID);

            SettingsEnum.SB_IS_VIP.saveValue(settingsJson.getBoolean("isVip"));
            SettingsEnum.SB_SHOW_TOAST_ON_SKIP.saveValue(!settingsJson.getBoolean("dontShowNotice"));
            SettingsEnum.SB_TRACK_SKIP_COUNT.saveValue(settingsJson.getBoolean("trackViewCount"));

            String serverAddress = settingsJson.getString("serverAddress");
            if (!isValidSBServerAddress(serverAddress)) {
                throw new IllegalArgumentException(str("sb_api_url_invalid"));
            }
            SettingsEnum.SB_API_URL.saveValue(serverAddress);

            SettingsEnum.SB_SHOW_TIME_WITHOUT_SEGMENTS.saveValue(settingsJson.getBoolean("showTimeWithSkips"));
            final float minDuration = (float)settingsJson.getDouble("minDuration");
            if (minDuration < 0) {
                throw new IllegalArgumentException("invalid minDuration: " + minDuration);
            }
            SettingsEnum.SB_MIN_DURATION.saveValue(minDuration);

            try {
                int skipCount = settingsJson.getInt("skipCount");
                if (skipCount < 0) {
                    throw new IllegalArgumentException("invalid skipCount: " + skipCount);
                }
                SettingsEnum.SB_SKIPPED_SEGMENTS_NUMBER_SKIPPED.saveValue(skipCount);

                final double minutesSaved = settingsJson.getDouble("minutesSaved");
                if (minutesSaved < 0) {
                    throw new IllegalArgumentException("invalid minutesSaved: " + minutesSaved);
                }
                SettingsEnum.SB_SKIPPED_SEGMENTS_TIME_SAVED.saveValue((long)(minutesSaved * 60 * 1000));
            } catch (JSONException ex) {
                // ignore. values were not exported in prior versions of ReVanced
            }

            ReVancedUtils.showToastLong(str("sb_settings_import_successful"));
        } catch (Exception ex) {
            LogHelper.printInfo(() -> "failed to import settings", ex); // use info level, as we are showing our own toast
            ReVancedUtils.showToastLong(str("sb_settings_import_failed", ex.getMessage()));
        }
    }

    @NonNull
    public static String exportSettings() {
        ReVancedUtils.verifyOnMainThread();
        try {
            LogHelper.printDebug(() -> "Creating SponsorBlock export settings string");
            JSONObject json = new JSONObject();

            JSONObject barTypesObject = new JSONObject(); // categories' colors
            JSONArray categorySelectionsArray = new JSONArray(); // categories' behavior

            SegmentCategory[] categories = SegmentCategory.categoriesWithoutUnsubmitted();
            for (SegmentCategory category : categories) {
                JSONObject categoryObject = new JSONObject();
                String categoryKey = category.key;
                categoryObject.put("color", category.colorString());
                barTypesObject.put(categoryKey, categoryObject);

                if (category.behaviour != CategoryBehaviour.IGNORE) {
                    JSONObject behaviorObject = new JSONObject();
                    behaviorObject.put("name", categoryKey);
                    behaviorObject.put("option", category.behaviour.desktopKey);
                    categorySelectionsArray.put(behaviorObject);
                }
            }
            json.put("userID", SettingsEnum.SB_UUID.getString());
            json.put("isVip", SettingsEnum.SB_IS_VIP.getBoolean());
            json.put("serverAddress", SettingsEnum.SB_API_URL.getString());
            json.put("dontShowNotice", !SettingsEnum.SB_SHOW_TOAST_ON_SKIP.getBoolean());
            json.put("showTimeWithSkips", SettingsEnum.SB_SHOW_TIME_WITHOUT_SEGMENTS.getBoolean());
            json.put("minDuration", SettingsEnum.SB_MIN_DURATION.getFloat());
            json.put("trackViewCount", SettingsEnum.SB_TRACK_SKIP_COUNT.getBoolean());
            json.put("skipCount", SettingsEnum.SB_SKIPPED_SEGMENTS_NUMBER_SKIPPED.getInt());
            json.put("minutesSaved", SettingsEnum.SB_SKIPPED_SEGMENTS_TIME_SAVED.getLong() / (60f * 1000));

            json.put("categorySelections", categorySelectionsArray);
            json.put("barTypes", barTypesObject);

            return json.toString(2);
        } catch (Exception ex) {
            LogHelper.printInfo(() -> "failed to export settings", ex); // use info level, as we are showing our own toast
            ReVancedUtils.showToastLong(str("sb_settings_export_failed"));
            return "";
        }
    }

    public static boolean isValidSBUserId(@NonNull String userId) {
        return !userId.isEmpty();
    }

    /**
     * A non comprehensive check if a SB api server address is valid.
     */
    public static boolean isValidSBServerAddress(@NonNull String serverAddress) {
        if (!Patterns.WEB_URL.matcher(serverAddress).matches()) {
            return false;
        }
        // Verify url is only the server address and does not contain a path such as: "https://sponsor.ajay.app/api/"
        // Could use Patterns.compile, but this is simpler
        final int lastDotIndex = serverAddress.lastIndexOf('.');
        if (lastDotIndex != -1 && serverAddress.substring(lastDotIndex).contains("/")) {
            return false;
        }
        // Optionally, could also verify the domain exists using "InetAddress.getByName(serverAddress)"
        // but that should not be done on the main thread.
        // Instead, assume the domain exists and the user knows what they're doing.
        return true;
    }

    private static boolean initialized;

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        String uuid = SettingsEnum.SB_UUID.getString();
        if (uuid.isEmpty()) {
            uuid = (UUID.randomUUID().toString() +
                    UUID.randomUUID().toString() +
                    UUID.randomUUID().toString())
                    .replace("-", "");
            SettingsEnum.SB_UUID.saveValue(uuid);
        }

        SegmentCategory.loadFromPreferences();
    }
}
