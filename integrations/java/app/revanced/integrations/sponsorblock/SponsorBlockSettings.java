package app.revanced.integrations.sponsorblock;

import static app.revanced.integrations.utils.StringRef.str;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    /**
     * Minimum length a SB user id must be, as set by SB API.
     */
    private static final int SB_PRIVATE_USER_ID_MINIMUM_LENGTH = 30;

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

            if (settingsJson.has("userID")) {
                // User id does not exist if user never voted or created any segments.
                String userID = settingsJson.getString("userID");
                if (isValidSBUserId(userID)) {
                    SettingsEnum.SB_PRIVATE_USER_ID.saveValue(userID);
                }
            }
            SettingsEnum.SB_USER_IS_VIP.saveValue(settingsJson.getBoolean("isVip"));
            SettingsEnum.SB_TOAST_ON_SKIP.saveValue(!settingsJson.getBoolean("dontShowNotice"));
            SettingsEnum.SB_TRACK_SKIP_COUNT.saveValue(settingsJson.getBoolean("trackViewCount"));
            SettingsEnum.SB_VIDEO_LENGTH_WITHOUT_SEGMENTS.saveValue(settingsJson.getBoolean("showTimeWithSkips"));

            String serverAddress = settingsJson.getString("serverAddress");
            if (isValidSBServerAddress(serverAddress)) { // Old versions of ReVanced exported wrong url format
                SettingsEnum.SB_API_URL.saveValue(serverAddress);
            }

            final float minDuration = (float) settingsJson.getDouble("minDuration");
            if (minDuration < 0) {
                throw new IllegalArgumentException("invalid minDuration: " + minDuration);
            }
            SettingsEnum.SB_SEGMENT_MIN_DURATION.saveValue(minDuration);

            if (settingsJson.has("skipCount")) { // Value not exported in old versions of ReVanced
                int skipCount = settingsJson.getInt("skipCount");
                if (skipCount < 0) {
                    throw new IllegalArgumentException("invalid skipCount: " + skipCount);
                }
                SettingsEnum.SB_LOCAL_TIME_SAVED_NUMBER_SEGMENTS.saveValue(skipCount);
            }

            if (settingsJson.has("minutesSaved")) {
                final double minutesSaved = settingsJson.getDouble("minutesSaved");
                if (minutesSaved < 0) {
                    throw new IllegalArgumentException("invalid minutesSaved: " + minutesSaved);
                }
                SettingsEnum.SB_LOCAL_TIME_SAVED_MILLISECONDS.saveValue((long) (minutesSaved * 60 * 1000));
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
            if (SponsorBlockSettings.userHasSBPrivateId()) {
                json.put("userID", SettingsEnum.SB_PRIVATE_USER_ID.getString());
            }
            json.put("isVip", SettingsEnum.SB_USER_IS_VIP.getBoolean());
            json.put("serverAddress", SettingsEnum.SB_API_URL.getString());
            json.put("dontShowNotice", !SettingsEnum.SB_TOAST_ON_SKIP.getBoolean());
            json.put("showTimeWithSkips", SettingsEnum.SB_VIDEO_LENGTH_WITHOUT_SEGMENTS.getBoolean());
            json.put("minDuration", SettingsEnum.SB_SEGMENT_MIN_DURATION.getFloat());
            json.put("trackViewCount", SettingsEnum.SB_TRACK_SKIP_COUNT.getBoolean());
            json.put("skipCount", SettingsEnum.SB_LOCAL_TIME_SAVED_NUMBER_SEGMENTS.getInt());
            json.put("minutesSaved", SettingsEnum.SB_LOCAL_TIME_SAVED_MILLISECONDS.getLong() / (60f * 1000));

            json.put("categorySelections", categorySelectionsArray);
            json.put("barTypes", barTypesObject);

            return json.toString(2);
        } catch (Exception ex) {
            LogHelper.printInfo(() -> "failed to export settings", ex); // use info level, as we are showing our own toast
            ReVancedUtils.showToastLong(str("sb_settings_export_failed", ex));
            return "";
        }
    }

    /**
     * Export the categories using flatten json (no embedded dictionaries or arrays).
     */
    public static void exportCategoriesToFlatJson(@Nullable Context dialogContext,
                                                  @NonNull JSONObject json) throws JSONException {
        ReVancedUtils.verifyOnMainThread();
        initialize();

        // If user has a SponsorBlock user id then show a warning.
        if (dialogContext != null && SponsorBlockSettings.userHasSBPrivateId()
                && !SettingsEnum.SB_HIDE_EXPORT_WARNING.getBoolean()) {
            new AlertDialog.Builder(dialogContext)
                    .setMessage(str("sb_settings_revanced_export_user_id_warning"))
                    .setNeutralButton(str("sb_settings_revanced_export_user_id_warning_dismiss"),
                            (dialog, which) -> SettingsEnum.SB_HIDE_EXPORT_WARNING.saveValue(true))
                    .setPositiveButton(android.R.string.ok, null)
                    .setCancelable(false)
                    .show();
        }

        for (SegmentCategory category : SegmentCategory.categoriesWithoutUnsubmitted()) {
            category.exportToFlatJSON(json);
        }
    }

    /**
     * Import the categories using flatten json (no embedded dictionaries or arrays).
     *
     * @return the number of settings imported
     */
    public static int importCategoriesFromFlatJson(JSONObject json) throws JSONException {
        ReVancedUtils.verifyOnMainThread();
        initialize();

        int numberOfImportedSettings = 0;
        SharedPreferences.Editor editor = SharedPrefCategory.SPONSOR_BLOCK.preferences.edit();
        for (SegmentCategory category : SegmentCategory.categoriesWithoutUnsubmitted()) {
            numberOfImportedSettings += category.importFromFlatJSON(json, editor);
        }
        editor.apply();

        SegmentCategory.updateEnabledCategories();

        return numberOfImportedSettings;
    }

    public static boolean isValidSBUserId(@NonNull String userId) {
        return !userId.isEmpty() && userId.length() >= SB_PRIVATE_USER_ID_MINIMUM_LENGTH;
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

    /**
     * @return if the user has ever voted, created a segment, or imported existing SB settings.
     */
    public static boolean userHasSBPrivateId() {
        return !SettingsEnum.SB_PRIVATE_USER_ID.getString().isEmpty();
    }

    /**
     * Use this only if a user id is required (creating segments, voting).
     */
    @NonNull
    public static String getSBPrivateUserID() {
        String uuid = SettingsEnum.SB_PRIVATE_USER_ID.getString();
        if (uuid.isEmpty()) {
            uuid = (UUID.randomUUID().toString() +
                    UUID.randomUUID().toString() +
                    UUID.randomUUID().toString())
                    .replace("-", "");
            SettingsEnum.SB_PRIVATE_USER_ID.saveValue(uuid);
        }
        return uuid;
    }

    private static boolean initialized;

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        SegmentCategory.loadFromPreferences();
    }
}
