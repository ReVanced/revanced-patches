package fi.razerman.youtube.VideoUrl;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;
import fi.razerman.youtube.XGlobals;
import fi.vanced.libraries.youtube.player.VideoHelpers;
import java.lang.ref.WeakReference;

/* loaded from: classes6.dex */
public class Copy {
    static String TAG = "CopyButton";
    static WeakReference<ImageView> _button = new WeakReference<>(null);
    static ConstraintLayout _constraintLayout;
    static int fadeDurationFast;
    static int fadeDurationScheduled;
    static Animation fadeIn;
    static Animation fadeOut;
    public static boolean isCopyButtonEnabled;
    static boolean isShowing;

    public static void initializeCopyButton(Object obj) {
        try {
            if (XGlobals.debug) {
                Log.d(TAG, "initializing");
            }
            _constraintLayout = (ConstraintLayout) obj;
            isCopyButtonEnabled = shouldBeShown();
            ImageView imageView = _constraintLayout.findViewById(getIdentifier("copy_button", "id"));
            if (XGlobals.debug && imageView == null) {
                Log.d(TAG, "Couldn't find imageView with id \"copy_button\"");
            }
            if (imageView != null) {
                // from class: fi.razerman.youtube.VideoUrl.Copy.1
// android.view.View.OnClickListener
                imageView.setOnClickListener(view -> {
                    if (XGlobals.debug) {
                        Log.d(Copy.TAG, "Button clicked");
                    }
                    VideoHelpers.copyVideoUrlToClipboard();
                });
                _button = new WeakReference<>(imageView);
                fadeDurationFast = getInteger("fade_duration_fast");
                fadeDurationScheduled = getInteger("fade_duration_scheduled");
                Animation animation = getAnimation("fade_in");
                fadeIn = animation;
                animation.setDuration(fadeDurationFast);
                Animation animation2 = getAnimation("fade_out");
                fadeOut = animation2;
                animation2.setDuration(fadeDurationScheduled);
                isShowing = true;
                changeVisibility(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Unable to set FrameLayout", e);
        }
    }

    public static void changeVisibility(boolean z) {
        if (isShowing != z) {
            isShowing = z;
            ImageView imageView = _button.get();
            if (_constraintLayout != null && imageView != null) {
                if (z && isCopyButtonEnabled) {
                    if (XGlobals.debug) {
                        Log.d(TAG, "Fading in");
                    }
                    imageView.setVisibility(View.VISIBLE);
                    imageView.startAnimation(fadeIn);
                } else if (imageView.getVisibility() == View.VISIBLE) {
                    if (XGlobals.debug) {
                        Log.d(TAG, "Fading out");
                    }
                    imageView.startAnimation(fadeOut);
                    imageView.setVisibility(View.GONE);
                }
            }
        }
    }

    public static void refreshShouldBeShown() {
        isCopyButtonEnabled = shouldBeShown();
    }

    private static boolean shouldBeShown() {
        Context appContext = YouTubeTikTokRoot_Application.getAppContext();
        if (appContext == null) {
            Log.e(TAG, "shouldBeShown - context is null!");
            return false;
        }
        String string = appContext.getSharedPreferences("youtube", 0).getString("pref_copy_video_url_button_list", null);
        if (string == null || string.isEmpty()) {
            return false;
        }
        return string.equalsIgnoreCase("PLAYER") || string.equalsIgnoreCase("BOTH");
    }

    private static int getIdentifier(String str, String str2) {
        Context appContext = YouTubeTikTokRoot_Application.getAppContext();
        return appContext.getResources().getIdentifier(str, str2, appContext.getPackageName());
    }

    private static int getInteger(String str) {
        return YouTubeTikTokRoot_Application.getAppContext().getResources().getInteger(getIdentifier(str, "integer"));
    }

    private static Animation getAnimation(String str) {
        return AnimationUtils.loadAnimation(YouTubeTikTokRoot_Application.getAppContext(), getIdentifier(str, "anim"));
    }
}
