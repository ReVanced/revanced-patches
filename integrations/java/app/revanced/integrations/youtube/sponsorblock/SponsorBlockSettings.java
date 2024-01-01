package app.revanced.integrations.youtube.sponsorblock;

import static app.revanced.integrations.shared.StringRef.str;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.shared.settings.Setting;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.sponsorblock.objects.CategoryBehaviour;
import app.revanced.integrations.youtube.sponsorblock.objects.SegmentCategory;

public class SponsorBlockSettings {
    /**
     * Minimum length a SB user id must be, as set by SB API.
     */
    private static final int SB_PRIVATE_USER_ID_MINIMUM_LENGTH = 30;

    public static void importDesktopSettings(@NonNull String json) {
        Utils.verifyOnMainThread();
        try {
            JSONObject settingsJson = new JSONObject(json);
            JSONObject barTypesObject = settingsJson.getJSONObject("barTypes");
            JSONArray categorySelectionsArray = settingsJson.getJSONArray("categorySelections");

            for (SegmentCategory category : SegmentCategory.categoriesWithoutUnsubmitted()) {
                // clear existing behavior, as browser plugin exports no behavior for ignored categories
                category.setBehaviour(CategoryBehaviour.IGNORE);
                if (barTypesObject.has(category.keyValue)) {
                    JSONObject categoryObject = barTypesObject.getJSONObject(category.keyValue);
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

                final int desktopValue = categorySelectionObject.getInt("option");
                CategoryBehaviour behaviour = CategoryBehaviour.byDesktopKeyValue(desktopValue);
                if (behaviour == null) {
                    Utils.showToastLong(categoryKey + " unknown desktop behavior value: " + desktopValue);
                } else if (category == SegmentCategory.HIGHLIGHT && behaviour == CategoryBehaviour.SKIP_AUTOMATICALLY_ONCE) {
                    Utils.showToastLong("Skip-once behavior not allowed for " + category.keyValue);
                    category.setBehaviour(CategoryBehaviour.SKIP_AUTOMATICALLY); // use closest match
                } else {
                    category.setBehaviour(behaviour);
                }
            }
            SegmentCategory.updateEnabledCategories();

            if (settingsJson.has("userID")) {
                // User id does not exist if user never voted or created any segments.
                String userID = settingsJson.getString("userID");
                if (isValidSBUserId(userID)) {
                    Settings.SB_PRIVATE_USER_ID.save(userID);
                }
            }
            Settings.SB_USER_IS_VIP.save(settingsJson.getBoolean("isVip"));
            Settings.SB_TOAST_ON_SKIP.save(!settingsJson.getBoolean("dontShowNotice"));
            Settings.SB_TRACK_SKIP_COUNT.save(settingsJson.getBoolean("trackViewCount"));
            Settings.SB_VIDEO_LENGTH_WITHOUT_SEGMENTS.save(settingsJson.getBoolean("showTimeWithSkips"));

            String serverAddress = settingsJson.getString("serverAddress");
            if (isValidSBServerAddress(serverAddress)) { // Old versions of ReVanced exported wrong url format
                Settings.SB_API_URL.save(serverAddress);
            }

            final float minDuration = (float) settingsJson.getDouble("minDuration");
            if (minDuration < 0) {
                throw new IllegalArgumentException("invalid minDuration: " + minDuration);
            }
            Settings.SB_SEGMENT_MIN_DURATION.save(minDuration);

            if (settingsJson.has("skipCount")) { // Value not exported in old versions of ReVanced
                int skipCount = settingsJson.getInt("skipCount");
                if (skipCount < 0) {
                    throw new IllegalArgumentException("invalid skipCount: " + skipCount);
                }
                Settings.SB_LOCAL_TIME_SAVED_NUMBER_SEGMENTS.save(skipCount);
            }

            if (settingsJson.has("minutesSaved")) {
                final double minutesSaved = settingsJson.getDouble("minutesSaved");
                if (minutesSaved < 0) {
                    throw new IllegalArgumentException("invalid minutesSaved: " + minutesSaved);
                }
                Settings.SB_LOCAL_TIME_SAVED_MILLISECONDS.save((long) (minutesSaved * 60 * 1000));
            }

            Utils.showToastLong(str("sb_settings_import_successful"));
        } catch (Exception ex) {
            Logger.printInfo(() -> "failed to import settings", ex); // use info level, as we are showing our own toast
            Utils.showToastLong(str("sb_settings_import_failed", ex.getMessage()));
        }
    }

    @NonNull
    public static String exportDesktopSettings() {
        Utils.verifyOnMainThread();
        try {
            Logger.printDebug(() -> "Creating SponsorBlock export settings string");
            JSONObject json = new JSONObject();

            JSONObject barTypesObject = new JSONObject(); // categories' colors
            JSONArray categorySelectionsArray = new JSONArray(); // categories' behavior

            SegmentCategory[] categories = SegmentCategory.categoriesWithoutUnsubmitted();
            for (SegmentCategory category : categories) {
                JSONObject categoryObject = new JSONObject();
                String categoryKey = category.keyValue;
                categoryObject.put("color", category.colorString());
                barTypesObject.put(categoryKey, categoryObject);

                if (category.behaviour != CategoryBehaviour.IGNORE) {
                    JSONObject behaviorObject = new JSONObject();
                    behaviorObject.put("name", categoryKey);
                    behaviorObject.put("option", category.behaviour.desktopKeyValue);
                    categorySelectionsArray.put(behaviorObject);
                }
            }
            if (SponsorBlockSettings.userHasSBPrivateId()) {
                json.put("userID", Settings.SB_PRIVATE_USER_ID.get());
            }
            json.put("isVip", Settings.SB_USER_IS_VIP.get());
            json.put("serverAddress", Settings.SB_API_URL.get());
            json.put("dontShowNotice", !Settings.SB_TOAST_ON_SKIP.get());
            json.put("showTimeWithSkips", Settings.SB_VIDEO_LENGTH_WITHOUT_SEGMENTS.get());
            json.put("minDuration", Settings.SB_SEGMENT_MIN_DURATION.get());
            json.put("trackViewCount", Settings.SB_TRACK_SKIP_COUNT.get());
            json.put("skipCount", Settings.SB_LOCAL_TIME_SAVED_NUMBER_SEGMENTS.get());
            json.put("minutesSaved", Settings.SB_LOCAL_TIME_SAVED_MILLISECONDS.get() / (60f * 1000));

            json.put("categorySelections", categorySelectionsArray);
            json.put("barTypes", barTypesObject);

            return json.toString(2);
        } catch (Exception ex) {
            Logger.printInfo(() -> "failed to export settings", ex); // use info level, as we are showing our own toast
            Utils.showToastLong(str("sb_settings_export_failed", ex));
            return "";
        }
    }

    /**
     * Export the categories using flatten json (no embedded dictionaries or arrays).
     */
    public static void showExportWarningIfNeeded(@Nullable Context dialogContext) {
        Utils.verifyOnMainThread();
        initialize();

        // If user has a SponsorBlock user id then show a warning.
        if (dialogContext != null && SponsorBlockSettings.userHasSBPrivateId()
                && !Settings.SB_HIDE_EXPORT_WARNING.get()) {
            new AlertDialog.Builder(dialogContext)
                    .setMessage(str("sb_settings_revanced_export_user_id_warning"))
                    .setNeutralButton(str("sb_settings_revanced_export_user_id_warning_dismiss"),
                            (dialog, which) -> Settings.SB_HIDE_EXPORT_WARNING.save(true))
                    .setPositiveButton(android.R.string.ok, null)
                    .setCancelable(false)
                    .show();
        }
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
        return !Settings.SB_PRIVATE_USER_ID.get().isEmpty();
    }

    /**
     * Use this only if a user id is required (creating segments, voting).
     */
    @NonNull
    public static String getSBPrivateUserID() {
        String uuid = Settings.SB_PRIVATE_USER_ID.get();
        if (uuid.isEmpty()) {
            uuid = (UUID.randomUUID().toString() +
                    UUID.randomUUID().toString() +
                    UUID.randomUUID().toString())
                    .replace("-", "");
            Settings.SB_PRIVATE_USER_ID.save(uuid);
        }
        return uuid;
    }

    private static boolean initialized;

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        SegmentCategory.updateEnabledCategories();
    }

    /**
     * Updates internal data based on {@link Setting} values.
     */
    public static void updateFromImportedSettings() {
        SegmentCategory.loadAllCategoriesFromSettings();
    }
}
