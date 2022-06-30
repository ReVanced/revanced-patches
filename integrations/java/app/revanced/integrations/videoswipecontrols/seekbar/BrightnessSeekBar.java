package app.revanced.integrations.videoswipecontrols.seekbar;

import android.content.Context;
import android.os.Handler;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.SharedPrefHelper;
import app.revanced.integrations.videoswipecontrols.helpers.BrightnessHelper;

/* loaded from: classes6.dex */
public class BrightnessSeekBar {
    public static final int MAX_BRIGHTNESS = 100;
    public static final int MIN_BRIGHTNESS = 0;
    public int Max;
    public int Progress;
    private boolean enabled;
    Handler handler;
    private final String mBrightnessKey = "revanced_brightness_value";
    Context mContext;
    TextView mTextView;
    ViewGroup mViewGroup;

    public void initialise(Context context, ViewGroup viewGroup) {
        this.enabled = false;
        this.mViewGroup = viewGroup;
        this.mContext = context;
        float systemBrightness = android.provider.Settings.System.getFloat(this.mContext.getContentResolver(), "screen_brightness", -1.0f);
        int _systemBrightness = (int) ((systemBrightness / 255.0f) * 100.0f);
        this.Progress = SharedPrefHelper.getInt(this.mContext, SharedPrefHelper.SharedPrefNames.YOUTUBE, "revanced_brightness_value", Integer.valueOf(_systemBrightness)).intValue();
        this.Max = 100;
        this.mTextView = new TextView(context);
        this.mTextView.setTextSize(24.0f);
        this.mTextView.setBackgroundColor(Integer.MIN_VALUE);
        this.mTextView.setTextColor(-1);
        this.mViewGroup.addView(this.mTextView);
    }

    /**
     * @param context YouTubePlayerOverlaysLayout.overlayContext
     */
    public void refreshViewGroup(ViewGroup viewGroup, Context context) {
        if (this.mTextView.getParent() != null) {
            ((ViewGroup) this.mTextView.getParent()).removeView(this.mTextView);
        }
        this.mContext = context;
        this.mViewGroup = viewGroup;
        this.mViewGroup.addView(this.mTextView);
    }

    private void updateBrightnessProgress() {
        this.Progress = BrightnessHelper.getBrightness(this.mContext);
        if (this.mTextView != null) {
            this.mTextView.setText("Brightness: " + this.Progress);
            if (!isVisible()) {
                this.mTextView.setVisibility(View.VISIBLE);
            }
        }
        LogHelper.debug("XDebug", "updateBrightnessProgress: " + this.Progress);
    }

    private void disableBrightness() {
        BrightnessHelper.setBrightness(this.mContext, -1);
    }

    public void setBrightness(int brightness) {
        if (this.enabled) {
            if (brightness < 0) {
                brightness = 0;
            } else if (brightness > 100) {
                brightness = 100;
            }
            BrightnessHelper.setBrightness(this.mContext, brightness);
            updateBrightnessProgress();
        }
    }

    public void manuallyUpdate(int update) {
        if (this.enabled) {
            setBrightness(update);
        }
    }

    public void hide() {
        if (isVisible()) {
            this.mTextView.setVisibility(View.INVISIBLE);
        }
    }

    public void hideDelayed() {
        if (this.handler == null) {
            this.handler = new Handler();
        }
        this.handler.postDelayed(new Runnable() { // from class: app.revanced.integrations.videoplayer.Fenster.Seekbar.BrightnessSeekBar.1
            @Override // java.lang.Runnable
            public void run() {
                BrightnessSeekBar.this.hide();
            }
        }, 2000L);
    }

    public boolean isVisible() {
        if (this.mTextView != null && this.mTextView.getVisibility() == View.VISIBLE) {
            return true;
        }
        return false;
    }

    public void disable() {
        this.enabled = false;
        SharedPrefHelper.saveInt(this.mContext, SharedPrefHelper.SharedPrefNames.YOUTUBE,"revanced_brightness_value", Integer.valueOf(this.Progress));
        disableBrightness();
        LogHelper.debug("XDebug", "Brightness swipe disabled");
    }

    public void enable() {
        this.enabled = true;
        float systemBrightness = android.provider.Settings.System.getFloat(this.mContext.getContentResolver(), "screen_brightness", -1.0f);
        int _systemBrightness = (int) ((systemBrightness / 255.0f) * 100.0f);
        int brightness = SharedPrefHelper.getInt(this.mContext, SharedPrefHelper.SharedPrefNames.YOUTUBE,"revanced_brightness_value", Integer.valueOf(_systemBrightness)).intValue();
        if (brightness < 0) {
            brightness = 0;
        } else if (brightness > 100) {
            brightness = 100;
        }
        BrightnessHelper.setBrightness(this.mContext, brightness);
        LogHelper.debug("XDebug", "Brightness swipe enabled");
    }
}
