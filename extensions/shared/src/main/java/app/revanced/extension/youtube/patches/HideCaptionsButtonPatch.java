package app.revanced.extension.youtube.patches;

import android.widget.ImageView;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class HideCaptionsButtonPatch {
    //Used by app.revanced.patches.youtube.layout.hidecaptionsbutton.patch.HideCaptionsButtonPatch
    public static void hideCaptionsButton(ImageView imageView) {
        imageView.setVisibility(Settings.HIDE_CAPTIONS_BUTTON.get() ? ImageView.GONE : ImageView.VISIBLE);
    }
}
