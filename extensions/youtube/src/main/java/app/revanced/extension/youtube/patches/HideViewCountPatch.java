package app.revanced.extension.youtube.patches;

import android.text.SpannableString;
import app.revanced.extension.youtube.settings.Settings;

public final class HideViewCountPatch {

    public static SpannableString hideViewCount(SpannableString text, float truncationDimension) {

        if (!Settings.HIDE_VIEW_COUNT.get()) {
            return text;
        }
        
        // provides the dimension which is 16f for the text below the title
        if (truncationDimension == 16f) {

            // it is sure that they contains " · "
            String[] words = text.toString().split(" · ");
            if (words.length != 3) {
                return text;
            }

            String modifiedText = words[0] + " · " + words[2];
            return new SpannableString(modifiedText);
        }
        return text;
    }

}