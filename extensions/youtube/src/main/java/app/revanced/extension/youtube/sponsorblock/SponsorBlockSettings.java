package app.revanced.extension.youtube.sponsorblock;

import static app.revanced.extension.shared.StringRef.str;

import android.app.Dialog;
import android.content.Context;
import android.util.Pair;
import android.util.Patterns;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;
import java.util.UUID;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.ui.CustomDialog;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.sponsorblock.objects.CategoryBehaviour;
import app.revanced.extension.youtube.sponsorblock.objects.SegmentCategory;
import app.revanced.extension.youtube.sponsorblock.ui.SponsorBlockPreferenceGroup;

@SuppressWarnings("NewApi")
public class SponsorBlockSettings {
    /**
     * Minimum length a SB user id must be, as set by SB API.
     */
    private static final int SB_PRIVATE_USER_ID_MINIMUM_LENGTH = 30;

    public static final Setting.ImportExportCallback SB_IMPORT_EXPORT_CALLBACK = new Setting.ImportExportCallback() {
        @Override
        public void settingsImported(@Nullable Context context) {
            SegmentCategory.loadAllCategoriesFromSettings();
            SponsorBlockPreferenceGroup.settingsImported = true;
        }
        @Override
        public void settingsExported(@Nullable Context context) {
            showExportWarningIfNeeded(context);
        }
    };

    public static void importDesktopSettings(@NonNull String json) {
        Utils.verifyOnMainThread();
        try {
            JSONObject settingsJson = new JSONObject(json);
            JSONObject barTypesObject = settingsJson.getJSONObject("barTypes");
            JSONArray categorySelectionsArray = settingsJson.getJSONArray("categorySelections");

            for (SegmentCategory category : SegmentCategory.categoriesWithoutUnsubmitted()) {
                // Clear existing behavior, as browser plugin exports no behavior for ignored categories.
                category.setBehaviour(CategoryBehaviour.IGNORE);
                if (barTypesObject.has(category.keyValue)) {
                    JSONObject categoryObject = barTypesObject.getJSONObject(category.keyValue);
                    // Older ReVanced SB exports lack an opacity value.
                    if (categoryObject.has("color") && categoryObject.has("opacity")) {
                        category.setColorWithOpacity(categoryObject.getString("color"));
                        category.setOpacity((float) categoryObject.getDouble("opacity"));
                    }
                }
            }

            for (int i = 0, length = categorySelectionsArray.length(); i < length; i++) {
                JSONObject categorySelectionObject = categorySelectionsArray.getJSONObject(i);

                String categoryKey = categorySelectionObject.getString("name");
                SegmentCategory category = SegmentCategory.byCategoryKey(categoryKey);
                if (category == null) {
                    continue; // Unsupported category, ignore.
                }

                final int desktopValue = categorySelectionObject.getInt("option");
                CategoryBehaviour behaviour = CategoryBehaviour.byDesktopKeyValue(desktopValue);
                if (behaviour == null) {
                    Utils.showToastLong(categoryKey + " unknown behavior key: " + categoryKey);
                } else if (category == SegmentCategory.HIGHLIGHT && behaviour == CategoryBehaviour.SKIP_AUTOMATICALLY_ONCE) {
                    Utils.showToastLong("Skip-once behavior not allowed for " + category.keyValue);
                    category.setBehaviour(CategoryBehaviour.SKIP_AUTOMATICALLY); // Use closest match.
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
            if (isValidSBServerAddress(serverAddress)) { // Old versions of ReVanced exported wrong url format.
                Settings.SB_API_URL.save(serverAddress);
            }

            final float minDuration = (float) settingsJson.getDouble("minDuration");
            if (minDuration < 0) {
                throw new IllegalArgumentException("invalid minDuration: " + minDuration);
            }
            Settings.SB_SEGMENT_MIN_DURATION.save(minDuration);

            if (settingsJson.has("skipCount")) { // Value not exported in old versions of ReVanced.
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

            Utils.showToastLong(str("revanced_sb_settings_import_successful"));
        } catch (Exception ex) {
            Logger.printInfo(() -> "failed to import settings", ex); // Use info level, as we are showing our own toast.
            Utils.showToastLong(str("revanced_sb_settings_import_failed", ex.getMessage()));
        }
    }

    @NonNull
    public static String exportDesktopSettings() {
        Utils.verifyOnMainThread();
        try {
            Logger.printDebug(() -> "Creating SponsorBlock export settings string");
            JSONObject json = new JSONObject();

            JSONObject barTypesObject = new JSONObject(); // Categories' colors.
            JSONArray categorySelectionsArray = new JSONArray(); // Categories' behavior.

            SegmentCategory[] categories = SegmentCategory.categoriesWithoutUnsubmitted();
            for (SegmentCategory category : categories) {
                JSONObject categoryObject = new JSONObject();
                String categoryKey = category.keyValue;
                // SB settings use separate color and opacity.
                categoryObject.put("color", category.getColorStringWithoutOpacity());
                categoryObject.put("opacity", category.getOpacity());
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
            Logger.printInfo(() -> "failed to export settings", ex); // Use info level, as we are showing our own toast.
            Utils.showToastLong(str("revanced_sb_settings_export_failed", ex));
            return "";
        }
    }

    /**
     * Export the categories using flatten json (no embedded dictionaries or arrays).
     */
    private static void showExportWarningIfNeeded(@Nullable Context dialogContext) {
        Utils.verifyOnMainThread();
        initialize();

        // If user has a SponsorBlock user id then show a warning.
        if (dialogContext != null && SponsorBlockSettings.userHasSBPrivateId()
                && !Settings.SB_HIDE_EXPORT_WARNING.get()) {
            // Create the custom dialog.
            Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                    dialogContext,
                    null, // No title.
                    str("revanced_sb_settings_revanced_export_user_id_warning"), // Message.
                    null, // No EditText.
                    null, // OK button text.
                    () -> {}, // OK button action (dismiss only).
                    null, // No cancel button action.
                    str("revanced_sb_settings_revanced_export_user_id_warning_dismiss"), // Neutral button text.
                    () -> Settings.SB_HIDE_EXPORT_WARNING.save(true), // Neutral button action.
                    true // Dismiss dialog when onNeutralClick.
            );

            // Set dialog as non-cancelable.
            dialogPair.first.setCancelable(false);

            // Show the dialog.
            dialogPair.first.show();
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
        // Could use Patterns.compile, but this is simpler.
        final int lastDotIndex = serverAddress.lastIndexOf('.');
        return lastDotIndex > 0 && !serverAddress.substring(lastDotIndex).contains("/");
        // Optionally, could also verify the domain exists using "InetAddress.getByName(serverAddress)"
        // but that should not be done on the main thread.
        // Instead, assume the domain exists and the user knows what they're doing.
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

    public static String migrateOldColorString(String colorString, float opacity) {
        if (colorString.length() >= 8) {
            return colorString;
        }

        // Change color string from #RGB to #ARGB using default alpha.
        if (colorString.startsWith("#")) {
            colorString = colorString.substring(1);
        }

        String alphaHex = String.format(Locale.US, "%02X", (int)(opacity * 255));
        String argbColorString = '#' + alphaHex + colorString.substring(0, 6);
        Logger.printDebug(() -> "Migrating old color string with default opacity: " + argbColorString);
        return argbColorString;
    }

    private static boolean initialized;

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        SegmentCategory.updateEnabledCategories();
    }
}
