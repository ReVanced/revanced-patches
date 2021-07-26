package pl.jakubweg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import pl.jakubweg.objects.SponsorSegment;

import static fi.vanced.libraries.youtube.sponsors.player.ui.SponsorBlockView.hideSkipButton;
import static fi.vanced.libraries.youtube.sponsors.player.ui.SponsorBlockView.showSkipButton;
import static pl.jakubweg.PlayerController.VERBOSE;

@SuppressLint({"RtlHardcoded", "SetTextI18n", "LongLogTag", "AppCompatCustomView"})
public class SkipSegmentView {
    public static final String TAG = "jakubweg.SkipSegmentView";
    private static SponsorSegment lastNotifiedSegment;

    public static void show() {
        showSkipButton();
    }

    public static void hide() {
        hideSkipButton();
    }

    public static void notifySkipped(SponsorSegment segment) {
        if (segment == lastNotifiedSegment) {
            if (VERBOSE)
                Log.d(TAG, "notifySkipped; segment == lastNotifiedSegment");
            return;
        }
        lastNotifiedSegment = segment;
        String skipMessage = segment.category.skipMessage.toString();
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (VERBOSE)
            Log.d(TAG, String.format("notifySkipped; message=%s", skipMessage));

        if (context != null)
            Toast.makeText(context, skipMessage, Toast.LENGTH_SHORT).show();
    }

    public static float convertDpToPixel(float dp, Context context) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
