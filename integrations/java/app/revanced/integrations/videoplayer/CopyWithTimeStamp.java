package app.revanced.integrations.videoplayer;

import android.content.Context;

import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.sponsorblock.player.VideoHelpers;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

import java.lang.ref.WeakReference;

/* loaded from: classes6.dex */
//ToDo: Refactor
public class CopyWithTimeStamp {
    static WeakReference<ImageView> _button = new WeakReference<>(null);
    static ConstraintLayout _constraintLayout;
    static int fadeDurationFast;
    static int fadeDurationScheduled;
    static Animation fadeIn;
    static Animation fadeOut;
    public static boolean isCopyButtonWithTimeStampEnabled;
    static boolean isShowing;

    public static void initializeCopyButtonWithTimeStamp(Object obj) {
        try {
            LogHelper.debug(CopyWithTimeStamp.class, "initializing");
            _constraintLayout = (ConstraintLayout) obj;
            isCopyButtonWithTimeStampEnabled = shouldBeShown();
            ImageView imageView = (ImageView) _constraintLayout.findViewById(getIdentifier("copy_with_timestamp_button", "id"));
            if (imageView == null) {
                LogHelper.debug(CopyWithTimeStamp.class, "Couldn't find imageView with id \"copy_with_timestamp_button\"");
            }
            if (imageView != null) {
                imageView.setOnClickListener(new View.OnClickListener() { // from class: app.revanced.integrations.videoplayer.VideoUrl.CopyWithTimeStamp.1
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        LogHelper.debug(CopyWithTimeStamp.class, "Button clicked");
                        VideoHelpers.copyVideoUrlWithTimeStampToClipboard();
                    }
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
            LogHelper.printException(CopyWithTimeStamp.class, "Unable to set FrameLayout", e);
        }
    }

    public static void changeVisibility(boolean z) {
        if (isShowing != z) {
            isShowing = z;
            ImageView imageView = _button.get();
            if (_constraintLayout != null && imageView != null) {
                if (z && isCopyButtonWithTimeStampEnabled) {
                    LogHelper.debug(CopyWithTimeStamp.class, "Fading in");
                    imageView.setVisibility(View.VISIBLE);
                    imageView.startAnimation(fadeIn);
                } else if (imageView.getVisibility() == View.VISIBLE) {
                    LogHelper.debug(CopyWithTimeStamp.class, "Fading out");
                    imageView.startAnimation(fadeOut);
                    imageView.setVisibility(View.GONE);
                }
            }
        }
    }

    public static void refreshShouldBeShown() {
        isCopyButtonWithTimeStampEnabled = shouldBeShown();
    }

    private static boolean shouldBeShown() {
        Context appContext = ReVancedUtils.getContext();
        if (appContext == null) {
            LogHelper.printException(CopyWithTimeStamp.class, "shouldBeShown - context is null!");
            return false;
        }

        String string = SharedPrefHelper.getString(appContext, SharedPrefHelper.SharedPrefNames.YOUTUBE, "pref_copy_video_url_timestamp_button_list", null);
        if (string == null || string.isEmpty()) {
            return false;
        }
        return string.equalsIgnoreCase("PLAYER") || string.equalsIgnoreCase("BOTH");
    }

    private static int getIdentifier(String str, String str2) {
        Context appContext = ReVancedUtils.getContext();
        return appContext.getResources().getIdentifier(str, str2, appContext.getPackageName());
    }

    private static int getInteger(String str) {
        return ReVancedUtils.getContext().getResources().getInteger(getIdentifier(str, "integer"));
    }

    private static Animation getAnimation(String str) {
        return AnimationUtils.loadAnimation(ReVancedUtils.getContext(), getIdentifier(str, "anim"));
    }
}
