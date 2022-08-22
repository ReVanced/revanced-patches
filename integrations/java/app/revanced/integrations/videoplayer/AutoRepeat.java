package app.revanced.integrations.videoplayer;

import android.content.Context;

import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

import java.lang.ref.WeakReference;

/* loaded from: classes6.dex */
//ToDo: Refactor
public class AutoRepeat {
    static WeakReference<ImageView> _autoRepeatBtn = new WeakReference<>(null);
    static ConstraintLayout _constraintLayout;
    static int fadeDurationFast;
    static int fadeDurationScheduled;
    static Animation fadeIn;
    static Animation fadeOut;
    public static boolean isAutoRepeatBtnEnabled;
    static boolean isShowing;

    public static void initializeAutoRepeat(Object constraintLayout) {
        try {
            LogHelper.debug(AutoRepeat.class, "initializing auto repeat");
            CopyWithTimeStamp.initializeCopyButtonWithTimeStamp(constraintLayout);
            Copy.initializeCopyButton(constraintLayout);
            _constraintLayout = (ConstraintLayout) constraintLayout;
            isAutoRepeatBtnEnabled = shouldBeShown();
            ImageView imageView = _constraintLayout.findViewById(getIdentifier("autoreplay_button", "id"));
            if (imageView == null) {
                LogHelper.debug(AutoRepeat.class, "Couldn't find imageView with tag \"autoreplay_button\"");
            }
            if (imageView != null) {
                imageView.setSelected(shouldBeSelected());
                imageView.setOnClickListener(new View.OnClickListener() { // from class: app.revanced.integrations.videoplayer.Autorepeat.AutoRepeat.1
                    @Override // android.view.View.OnClickListener
                    public void onClick(View v) {
                        LogHelper.debug(AutoRepeat.class, "Auto repeat button clicked");
                        AutoRepeat.changeSelected(!v.isSelected());
                    }
                });
                _autoRepeatBtn = new WeakReference<>(imageView);
                fadeDurationFast = getInteger("fade_duration_fast");
                fadeDurationScheduled = getInteger("fade_duration_scheduled");
                fadeIn = getAnimation("fade_in");
                fadeIn.setDuration(fadeDurationFast);
                fadeOut = getAnimation("fade_out");
                fadeOut.setDuration(fadeDurationScheduled);
                isShowing = true;
                changeVisibility(false);
            }
        } catch (Exception ex) {
            LogHelper.printException(AutoRepeat.class, "Unable to set FrameLayout", ex);
        }
    }

    public static void changeVisibility(boolean visible) {
        CopyWithTimeStamp.changeVisibility(visible);
        Copy.changeVisibility(visible);
        if (isShowing != visible) {
            isShowing = visible;
            ImageView iView = _autoRepeatBtn.get();
            if (_constraintLayout != null && iView != null) {
                if (visible && isAutoRepeatBtnEnabled) {
                    LogHelper.debug(AutoRepeat.class, "Fading in");
                    iView.setVisibility(View.VISIBLE);
                    iView.startAnimation(fadeIn);
                } else if (iView.getVisibility() == View.VISIBLE) {
                    LogHelper.debug(AutoRepeat.class, "Fading out");
                    iView.startAnimation(fadeOut);
                    iView.setVisibility(View.GONE);
                }
            }
        }
    }

    public static void changeSelected(boolean selected) {
        changeSelected(selected, false);
    }

    public static void changeSelected(boolean selected, boolean onlyView) {
        ImageView iView = _autoRepeatBtn.get();
        if (_constraintLayout != null && iView != null) {
            if (SettingsEnum.DEBUG.getBoolean()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Changing selected state to: ");
                sb.append(selected ? "SELECTED" : "NONE");
                LogHelper.debug(AutoRepeat.class, sb.toString());
            }
            iView.setSelected(selected);
            if (!onlyView) {
                setSelected(selected);
            }
        }
    }

    private static boolean shouldBeSelected() {
        Context context = ReVancedUtils.getContext();
        if (context == null) {
            LogHelper.printException(AutoRepeat.class, "ChangeSelected - context is null!");
            return false;
        }
        return SharedPrefHelper.getBoolean(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, "pref_auto_repeat", false);
    }

    private static void setSelected(boolean selected) {
        Context context = ReVancedUtils.getContext();
        SharedPrefHelper.saveBoolean(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, "pref_auto_repeat", selected);
    }

    private static boolean shouldBeShown() {
        Context context = ReVancedUtils.getContext();
        if (context == null) {
            LogHelper.printException(AutoRepeat.class, "ChangeSelected - context is null!");
            return false;
        }
        return SharedPrefHelper.getBoolean(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, "pref_auto_repeat_button", false);
    }

    private static int getIdentifier(String name, String defType) {
        Context context = ReVancedUtils.getContext();
        return context.getResources().getIdentifier(name, defType, context.getPackageName());
    }

    private static int getInteger(String name) {
        Context context = ReVancedUtils.getContext();
        return context.getResources().getInteger(getIdentifier(name, "integer"));
    }

    private static Animation getAnimation(String name) {
        Context context = ReVancedUtils.getContext();
        return AnimationUtils.loadAnimation(context, getIdentifier(name, "anim"));
    }
}
