package fi.razerman.youtube.Fenster.Seekbar;

import android.content.Context;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.apps.youtube.app.common.player.overlay.YouTubePlayerOverlaysLayout;
import fi.razerman.youtube.Fenster.Helpers.BrightnessHelper;
import fi.razerman.youtube.Helpers.SharedPrefs;
import fi.razerman.youtube.XGlobals;

/* loaded from: classes6.dex */
public class BrightnessSeekBar {
    public static final int MAX_BRIGHTNESS = 100;
    public static final int MIN_BRIGHTNESS = 0;
    public static final String TAG = "XDebug";
    public int Max;
    public int Progress;
    private boolean enabled;
    Handler handler;
    private final String mBrightnessKey = "xfile_brightness_value";
    Context mContext;
    TextView mTextView;
    ViewGroup mViewGroup;

    public void initialise(Context context, ViewGroup viewGroup) {
        this.enabled = false;
        this.mViewGroup = viewGroup;
        this.mContext = context;
        float systemBrightness = Settings.System.getFloat(this.mContext.getContentResolver(), "screen_brightness", -1.0f);
        int _systemBrightness = (int) ((systemBrightness / 255.0f) * 100.0f);
        this.Progress = SharedPrefs.getInt(this.mContext, "xfile_brightness_value", Integer.valueOf(_systemBrightness)).intValue();
        this.Max = 100;
        this.mTextView = new TextView(context);
        this.mTextView.setTextSize(24.0f);
        this.mTextView.setBackgroundColor(Integer.MIN_VALUE);
        this.mTextView.setTextColor(-1);
        this.mViewGroup.addView(this.mTextView);
    }

    public void refreshViewGroup(ViewGroup viewGroup) {
        if (this.mTextView.getParent() != null) {
            ((ViewGroup) this.mTextView.getParent()).removeView(this.mTextView);
        }
        this.mContext = YouTubePlayerOverlaysLayout.overlayContext;
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
        if (XGlobals.debug) {
            Log.d("XDebug", "updateBrightnessProgress: " + this.Progress);
        }
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
        this.handler.postDelayed(new Runnable() { // from class: fi.razerman.youtube.Fenster.Seekbar.BrightnessSeekBar.1
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
        SharedPrefs.saveInt(this.mContext, "xfile_brightness_value", Integer.valueOf(this.Progress));
        disableBrightness();
        Log.d("XDebug", "Brightness swipe disabled");
    }

    public void enable() {
        this.enabled = true;
        float systemBrightness = Settings.System.getFloat(this.mContext.getContentResolver(), "screen_brightness", -1.0f);
        int _systemBrightness = (int) ((systemBrightness / 255.0f) * 100.0f);
        int brightness = SharedPrefs.getInt(this.mContext, "xfile_brightness_value", Integer.valueOf(_systemBrightness)).intValue();
        if (brightness < 0) {
            brightness = 0;
        } else if (brightness > 100) {
            brightness = 100;
        }
        BrightnessHelper.setBrightness(this.mContext, brightness);
        Log.d("XDebug", "Brightness swipe enabled");
    }
}
