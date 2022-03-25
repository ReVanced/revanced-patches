package fi.razerman.youtube.Autorepeat;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;
import fi.razerman.youtube.VideoUrl.Copy;
import fi.razerman.youtube.VideoUrl.CopyWithTimeStamp;
import fi.razerman.youtube.XGlobals;
import java.lang.ref.WeakReference;

/* loaded from: classes6.dex */
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
            if (XGlobals.debug) {
                Log.d("AutoRepeat", "initializing auto repeat");
            }
            CopyWithTimeStamp.initializeCopyButtonWithTimeStamp(constraintLayout);
            Copy.initializeCopyButton(constraintLayout);
            _constraintLayout = (ConstraintLayout) constraintLayout;
            isAutoRepeatBtnEnabled = shouldBeShown();
            ImageView imageView = _constraintLayout.findViewById(getIdentifier("autoreplay_button", "id"));
            if (XGlobals.debug && imageView == null) {
                Log.d("AutoRepeat", "Couldn't find imageView with tag \"autoreplay_button\"");
            }
            if (imageView != null) {
                imageView.setSelected(shouldBeSelected());
                imageView.setOnClickListener(new View.OnClickListener() { // from class: fi.razerman.youtube.Autorepeat.AutoRepeat.1
                    @Override // android.view.View.OnClickListener
                    public void onClick(View v) {
                        if (XGlobals.debug) {
                            Log.d("AutoRepeat", "Auto repeat button clicked");
                        }
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
            Log.e("XError", "Unable to set FrameLayout", ex);
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
                    if (XGlobals.debug) {
                        Log.d("AutoRepeat", "Fading in");
                    }
                    iView.setVisibility(View.VISIBLE);
                    iView.startAnimation(fadeIn);
                } else if (iView.getVisibility() == View.VISIBLE) {
                    if (XGlobals.debug) {
                        Log.d("AutoRepeat", "Fading out");
                    }
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
            if (XGlobals.debug) {
                StringBuilder sb = new StringBuilder();
                sb.append("Changing selected state to: ");
                sb.append(selected ? "SELECTED" : "NONE");
                Log.d("AutoRepeat", sb.toString());
            }
            iView.setSelected(selected);
            if (!onlyView) {
                setSelected(selected);
            }
        }
    }

    private static boolean shouldBeSelected() {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (context == null) {
            Log.e("AutoRepeat", "ChangeSelected - context is null!");
            return false;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
        return sharedPreferences.getBoolean("pref_auto_repeat", false);
    }

    private static void setSelected(boolean selected) {
        try {
            Context context = YouTubeTikTokRoot_Application.getAppContext();
            if (context == null) {
                Log.e("AutoRepeat", "ChangeSelected - context is null!");
                return;
            }
            SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
            sharedPreferences.edit().putBoolean("pref_auto_repeat", selected).apply();
        } catch (Exception ignored) {
        }
    }

    private static boolean shouldBeShown() {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (context == null) {
            Log.e("AutoRepeat", "ChangeSelected - context is null!");
            return false;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
        return sharedPreferences.getBoolean("pref_auto_repeat_button", false);
    }

    private static int getIdentifier(String name, String defType) {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        return context.getResources().getIdentifier(name, defType, context.getPackageName());
    }

    private static int getInteger(String name) {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        return context.getResources().getInteger(getIdentifier(name, "integer"));
    }

    private static Animation getAnimation(String name) {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        return AnimationUtils.loadAnimation(context, getIdentifier(name, "anim"));
    }
}
