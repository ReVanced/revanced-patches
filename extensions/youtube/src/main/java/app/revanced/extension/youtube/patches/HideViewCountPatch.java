package app.revanced.extension.youtube.patches;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class HideViewCountPatch {

    /**
     * Injection point.
     */
    public static SpannableString modifyFeedSubtitleSpan(SpannableString original, float truncationDimension) {
        try {
            final boolean hideViewCount = Settings.HIDE_VIEW_COUNT.get();
            final boolean hideUploadTime = Settings.HIDE_UPLOAD_TIME.get();
            if (!hideViewCount && !hideUploadTime) {
                return original;
            }

            // Applies only for these specific dimensions.
            if (truncationDimension == 16f || truncationDimension == 42f) {
                String delimiter = " · ";
                final int delimiterLength = delimiter.length();

                // Find the first and second delimiter positions
                final int delimiterIndex1 = TextUtils.indexOf(original, delimiter);
                if (delimiterIndex1 < 0) return original;

                final int delimiterIndex2 = TextUtils.indexOf(original, delimiter,
                        delimiterIndex1 + delimiterLength);
                if (delimiterIndex2 < 0) return original;

                // Ensure there is exactly 2 delimiters.
                final int delimiterIndex3 = TextUtils.indexOf(original, delimiter,
                        delimiterIndex2 + delimiterLength);
                if (delimiterIndex3 >= 0) return original;

                // Make a mutable copy that keeps existing span styling.
                SpannableStringBuilder builder = new SpannableStringBuilder(original);

                // Remove the sections.
                if (hideUploadTime) {
                    builder.delete(delimiterIndex2, original.length());
                }

                if (hideViewCount) {
                    builder.delete(delimiterIndex1, delimiterIndex2);
                }

                SpannableString replacement = new SpannableString(builder);
                Logger.printDebug(() -> "Replacing feed subtitle span: " + original + " with: " + replacement);

                return replacement;
            }
        } catch (Exception ex) {
            Logger.printException(() -> "hideViewCount failure", ex);
        }

        return original;
    }

}