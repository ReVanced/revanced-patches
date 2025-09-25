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
    public static SpannableString hideViewCount(SpannableString original, float truncationDimension) {
        try {
            if (!Settings.HIDE_VIEW_COUNT.get()) {
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

                // Make a mutable copy that keeps all existing spans.
                SpannableStringBuilder builder = new SpannableStringBuilder(original);

                // Remove the view count section.
                builder.delete(delimiterIndex1 + delimiterLength, delimiterIndex2 + delimiterLength);

                SpannableString replacement = new SpannableString(builder);
                Logger.printDebug(() -> "Replacing view count span: " + original + " with: " + replacement);

                return replacement;
            }
        } catch (Exception ex) {
            Logger.printException(() -> "hideViewCount failure", ex);
        }

        return original;
    }

}