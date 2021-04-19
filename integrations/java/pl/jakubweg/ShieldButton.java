package pl.jakubweg;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import java.lang.ref.WeakReference;

import static fi.razerman.youtube.XGlobals.debug;

public class ShieldButton {
    static String TAG = "SHIELD";
    static RelativeLayout _youtubeControlsLayout;
    static WeakReference<ImageView> _shieldBtn = new WeakReference<>(null);
    static int fadeDurationFast;
    static int fadeDurationScheduled;
    static Animation fadeIn;
    static Animation fadeOut;
    static boolean isShowing;

    public static void initialize(Object viewStub) {
        try {
            if(debug){
                Log.d(TAG, "initializing shield button");
            }

            _youtubeControlsLayout = (RelativeLayout) viewStub;
            initButtonVisibilitySettings();

            ImageView imageView = (ImageView)_youtubeControlsLayout
                    .findViewById(getIdentifier("sponsorblock_button", "id"));

            if (debug && imageView == null){
                Log.d(TAG, "Couldn't find imageView with tag \"sponsorblock_button\"");
            }
            if (imageView == null) return;
            imageView.setOnClickListener(SponsorBlockUtils.sponsorBlockBtnListener);
            _shieldBtn = new WeakReference<>(imageView);

            // Animations
            fadeDurationFast = getInteger("fade_duration_fast");
            fadeDurationScheduled = getInteger("fade_duration_scheduled");
            fadeIn = getAnimation("fade_in");
            fadeIn.setDuration(fadeDurationFast);
            fadeOut = getAnimation("fade_out");
            fadeOut.setDuration(fadeDurationScheduled);
            isShowing = true;
            changeVisibilityImmediate(false);
        }
        catch (Exception ex) {
            Log.e(TAG, "Unable to set RelativeLayout", ex);
        }
    }

    public static void changeVisibilityImmediate(boolean visible) {
        changeVisibility(visible, true);
    }

    public static void changeVisibilityNegatedImmediate(boolean visible) {
        changeVisibility(!visible, true);
    }

    public static void changeVisibility(boolean visible) {
        changeVisibility(visible, false);
    }

    public static void changeVisibility(boolean visible, boolean immediate) {
        if (isShowing == visible) return;
        isShowing = visible;

        ImageView iView = _shieldBtn.get();
        if (_youtubeControlsLayout == null || iView == null) return;

        if (visible && shouldBeShown()) {
            if (debug) {
                Log.d(TAG, "Fading in");
            }
            iView.setVisibility(View.VISIBLE);
            if (!immediate)
                iView.startAnimation(fadeIn);
            return;
        }

        if (iView.getVisibility() == View.VISIBLE) {
            if (debug) {
                Log.d(TAG, "Fading out");
            }
            if (!immediate)
                iView.startAnimation(fadeOut);
            iView.setVisibility(shouldBeShown() ? View.INVISIBLE : View.GONE);
        }
    }

    private static boolean shouldBeShown() {
        return SponsorBlockSettings.isSponsorBlockEnabled && SponsorBlockSettings.isAddNewSegmentEnabled;
    }

    private static void initButtonVisibilitySettings() {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if(context == null){
            Log.e(TAG, "context is null");
            SponsorBlockSettings.isSponsorBlockEnabled = false;
            SponsorBlockSettings.isAddNewSegmentEnabled = false;
            return;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(SponsorBlockSettings.PREFERENCES_NAME, Context.MODE_PRIVATE);
        SponsorBlockSettings.isSponsorBlockEnabled = sharedPreferences.getBoolean(SponsorBlockSettings.PREFERENCES_KEY_SPONSOR_BLOCK_ENABLED, false);
        SponsorBlockSettings.isAddNewSegmentEnabled = sharedPreferences.getBoolean(SponsorBlockSettings.PREFERENCES_KEY_NEW_SEGMENT_ENABLED, false);
    }

    //region Helpers
    private static int getIdentifier(String name, String defType) {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        return context.getResources().getIdentifier(name, defType, context.getPackageName());
    }

    private static int getInteger(String name) {
        return YouTubeTikTokRoot_Application.getAppContext().getResources().getInteger(getIdentifier(name, "integer"));
    }

    private static Animation getAnimation(String name) {
        return AnimationUtils.loadAnimation(YouTubeTikTokRoot_Application.getAppContext(), getIdentifier(name, "anim"));
    }
    //endregion
}
