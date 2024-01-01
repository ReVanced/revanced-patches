package app.revanced.integrations.youtube.patches;

import android.widget.ImageView;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class HideCaptionsButtonPatch {
    //Used by app.revanced.patches.youtube.layout.hidecaptionsbutton.patch.HideCaptionsButtonPatch
    public static void hideCaptionsButton(ImageView imageView) {
        imageView.setVisibility(Settings.HIDE_CAPTIONS_BUTTON.get() ? ImageView.GONE : ImageView.VISIBLE);
    }
}
