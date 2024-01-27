package app.revanced.integrations.all.screencapture.removerestriction;

import android.media.AudioAttributes;
import android.os.Build;

import androidx.annotation.RequiresApi;

public final class RemoveScreencaptureRestrictionPatch {
    // Member of AudioAttributes.Builder
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static AudioAttributes.Builder setAllowedCapturePolicy(final AudioAttributes.Builder builder, final int capturePolicy) {
        builder.setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_ALL);

        return builder;
    }

    // Member of AudioManager static class
    public static void setAllowedCapturePolicy(final int capturePolicy) {
        // Ignore request
    }
}
