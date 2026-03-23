package app.revanced.extension.instagram.profile;

import android.widget.TextView;

@SuppressWarnings("unused")
public class CopyBioTextPatch {

    /**
     * Injection point.
     * Makes the bio TextView selectable and copyable.
     */
    public static void makeBioTextSelectable(TextView bioTextView) {
        if (bioTextView != null) {
            bioTextView.setTextIsSelectable(true);
        }
    }
}
