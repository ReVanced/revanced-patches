package app.revanced.extension.youtube.patches;

import android.text.SpannableString;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class HideViewCountPatch {

    /**
     * Injection point.
     */
    public static SpannableString hideViewCount(SpannableString text, float truncationDimension) {
        try {
            if (!Settings.HIDE_VIEW_COUNT.get()) {
                return text;
            }

            // provides the dimension which is 16f for the text below the title
            if (truncationDimension == 16f || truncationDimension == 42f) {
                String delimiter = " · ";

                String[] words = text.toString().split(delimiter);
                if (words.length != 3) {
                    return text;
                }

                SpannableString modifiedText = new SpannableString(words[0] + delimiter + words[2]);
                Logger.printDebug(() -> "Replacing view count span with: " + modifiedText);

                return modifiedText;
            }
        } catch (Exception ex) {
            Logger.printException(() -> "hideViewCount failure", ex);
        }

        return text;
    }

}