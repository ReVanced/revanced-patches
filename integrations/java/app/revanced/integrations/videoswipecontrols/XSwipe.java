package app.revanced.integrations.videoswipecontrols;

import android.content.Context;
import android.os.Handler;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.videoswipecontrols.seekbar.BrightnessSeekBar;
import app.revanced.integrations.videoswipecontrols.seekbar.VolumeSeekBar;
import app.revanced.integrations.utils.SwipeHelper;
import app.revanced.integrations.settingsmenu.ReVancedSettingsFragment;

/* loaded from: classes6.dex */
public class XSwipe implements SwipeEventsListener {
    public static final int ONE_FINGER = 1;
    Handler handler;
    float mBrightnessDownPos;
    int mBrightnessDownProgress;
    ViewGroup mViewGroup;
    float mVolumeDownPos;
    int mVolumeDownProgress;
    float mTouchProgressOffset = 0.0f;
    protected int mPaddingTop = 0;
    protected int mPaddingBottom = 0;
    protected int mPaddingLeft = 0;
    protected int mPaddingRight = 0;
    Orientation brightnessOrientation = Orientation.VERTICAL;
    Orientation volumeOrientation = Orientation.VERTICAL;
    Coverage brightnessCoverage = Coverage.LEFT;
    Coverage volumeCoverage = Coverage.RIGHT;
    BrightnessSeekBar mBrightness = new BrightnessSeekBar();
    VolumeSeekBar mVolume = new VolumeSeekBar();

    public XSwipe(Context context, ViewGroup viewGroup) {
        this.mViewGroup = viewGroup;
        this.mBrightness.initialise(context, viewGroup);
        this.mVolume.initialise(context, viewGroup);
    }

    @Override // app.revanced.integrations.videoplayer.Fenster.FensterEventsListener
    public void onTap() {
        LogHelper.debug("XDebug", "onTap");
    }

    @Override // app.revanced.integrations.videoplayer.Fenster.FensterEventsListener
    public void onHorizontalScroll(MotionEvent event, float delta) {
        LogHelper.debug("XDebug", "onHorizontalScroll - y: " + ((int) event.getY()) + " x: " + ((int) event.getX()));
        if (event.getPointerCount() == 1) {
            if (this.brightnessOrientation == Orientation.HORIZONTAL && (this.brightnessCoverage == Coverage.FULL || getCoverageHorizontal(event) == this.brightnessCoverage)) {
                updateBrightnessProgressBarHorizontal(event);
            }
            if (this.volumeOrientation != Orientation.HORIZONTAL) {
                return;
            }
            if (this.volumeCoverage == Coverage.FULL || getCoverageHorizontal(event) == this.volumeCoverage) {
                updateVolumeProgressBarHorizontal(event);
            }
        }
    }

    @Override // app.revanced.integrations.videoplayer.Fenster.FensterEventsListener
    public void onVerticalScroll(MotionEvent event, float delta) {
        LogHelper.debug("XDebug", "onVerticalScroll - y: " + ((int) event.getY()) + " x: " + ((int) event.getX()));
        if (event.getPointerCount() == 1) {
            if (this.brightnessOrientation == Orientation.VERTICAL && (this.brightnessCoverage == Coverage.FULL || getCoverageVertical(event) == this.brightnessCoverage)) {
                updateBrightnessProgressBarVertical(event);
            }
            if (this.volumeOrientation != Orientation.VERTICAL) {
                return;
            }
            if (this.volumeCoverage == Coverage.FULL || getCoverageVertical(event) == this.volumeCoverage) {
                updateVolumeProgressBarVertical(event);
            }
        }
    }

    @Override // app.revanced.integrations.videoplayer.Fenster.FensterEventsListener
    public void onSwipeRight() {
        LogHelper.debug("XDebug", "onSwipeRight");
    }

    @Override // app.revanced.integrations.videoplayer.Fenster.FensterEventsListener
    public void onSwipeLeft() {
        LogHelper.debug("XDebug", "onSwipeLeft");
    }

    @Override // app.revanced.integrations.videoplayer.Fenster.FensterEventsListener
    public void onSwipeBottom() {
        LogHelper.debug("XDebug", "onSwipeBottom");
    }

    @Override // app.revanced.integrations.videoplayer.Fenster.FensterEventsListener
    public void onSwipeTop() {
        LogHelper.debug("XDebug", "onSwipeTop");
    }

    @Override // app.revanced.integrations.videoplayer.Fenster.FensterEventsListener
    public void onDown(MotionEvent event) {
        LogHelper.debug("XDebug", "onDown");
        if (event.getPointerCount() == 1) {
            if (this.brightnessOrientation == Orientation.VERTICAL && (this.brightnessCoverage == Coverage.FULL || getCoverageVertical(event) == this.brightnessCoverage)) {
                this.mBrightnessDownPos = getProgressVertical(event, this.mBrightness.Max);
            }
            if (this.volumeOrientation == Orientation.VERTICAL && (this.volumeCoverage == Coverage.FULL || getCoverageVertical(event) == this.volumeCoverage)) {
                this.mVolumeDownPos = getProgressVertical(event, this.mVolume.Max);
            }
            if (this.brightnessOrientation == Orientation.HORIZONTAL && (this.brightnessCoverage == Coverage.FULL || getCoverageHorizontal(event) == this.brightnessCoverage)) {
                this.mBrightnessDownPos = getProgressHorizontal(event, this.mBrightness.Max);
            }
            if (this.volumeOrientation == Orientation.HORIZONTAL && (this.volumeCoverage == Coverage.FULL || getCoverageHorizontal(event) == this.volumeCoverage)) {
                this.mVolumeDownPos = getProgressHorizontal(event, this.mVolume.Max);
            }
            this.mVolumeDownProgress = this.mVolume.Progress;
            this.mBrightnessDownProgress = this.mBrightness.Progress;
        }
    }

    @Override // app.revanced.integrations.videoplayer.Fenster.FensterEventsListener
    public void onUp() {
        LogHelper.debug("XDebug", "onUp");
        hideNotifications();
    }

    public void disable() {
        if (this.mBrightness != null) {
            this.mBrightness.disable();
        }
        if (this.mVolume != null) {
            this.mVolume.disable();
        }
        hideNotifications();
    }

    public void enable(boolean brightness, boolean volume) {
        checkPlayerOverlaysView();
        if (brightness && this.mBrightness != null) {
            this.mBrightness.enable();
        }
        if (volume && this.mVolume != null) {
            this.mVolume.enable();
        }
    }

    public void hideNotifications() {
        if (this.handler == null) {
            this.handler = new Handler();
        }
        // from class: app.revanced.integrations.videoplayer.Fenster.XFenster.1
// java.lang.Runnable
        this.handler.postDelayed(() -> {
            XSwipe.this.mVolume.hide();
            XSwipe.this.mBrightness.hide();
        }, 2000L);
    }

    private void updateVolumeProgressBarVertical(MotionEvent event) {
        float difference = getDifferenceVertical(this.mVolumeDownPos, getProgressVertical(event, this.mVolume.Max));
        if (this.mBrightness.isVisible()) {
            this.mBrightness.hide();
        }
        this.mVolume.manuallyUpdate((int) (this.mVolumeDownProgress + difference));
    }

    private void updateBrightnessProgressBarVertical(MotionEvent event) {
        float difference = getDifferenceVertical(this.mBrightnessDownPos, getProgressVertical(event, this.mBrightness.Max));
        if (this.mVolume.isVisible()) {
            this.mVolume.hide();
        }
        this.mBrightness.manuallyUpdate((int) (this.mBrightnessDownProgress + difference));
    }

    private void updateVolumeProgressBarHorizontal(MotionEvent event) {
        float difference = getDifferenceHorizontal(this.mVolumeDownPos, getProgressHorizontal(event, this.mVolume.Max));
        if (this.mBrightness.isVisible()) {
            this.mBrightness.hide();
        }
        this.mVolume.manuallyUpdate((int) (this.mVolumeDownProgress + difference));
    }

    private void updateBrightnessProgressBarHorizontal(MotionEvent event) {
        float difference = getDifferenceHorizontal(this.mBrightnessDownPos, getProgressHorizontal(event, this.mBrightness.Max));
        if (this.mVolume.isVisible()) {
            this.mVolume.hide();
        }
        this.mBrightness.manuallyUpdate((int) (this.mBrightnessDownProgress + difference));
    }

    private float getDifferenceVertical(float downProgress, float newProgress) {
        float diff = downProgress - newProgress;
        return diff * (-1.0f);
    }

    private float getDifferenceHorizontal(float downProgress, float newProgress) {
        float diff = downProgress - newProgress;
        return diff;
    }

    private float getProgressVertical(MotionEvent event, int maxSteps) {
        float progress = calculateProgressVertical(event, maxSteps);
        LogHelper.debug("XDebug", "Progress vertical: " + progress);
        return progress;
    }

    private float getProgressHorizontal(MotionEvent event, int maxSteps) {
        float progress = calculateProgressHorizontal(event, maxSteps);
        LogHelper.debug("XDebug", "Progress horizontal: " + progress);
        return progress;
    }

    private float calculateProgressVertical(MotionEvent event, int maxSteps) {
        float scale;
        int height = this.mViewGroup.getHeight();
        LogHelper.debug("XDebug", "calculateProgressVertical - height: " + height);
        int available = (height - this.mPaddingTop) - this.mPaddingBottom;
        int y = height - ((int) event.getY());
        float progress = 0.0f;
        if (y < this.mPaddingBottom) {
            scale = 0.0f;
        } else if (y > height - this.mPaddingTop) {
            scale = 1.0f;
        } else {
            scale = (y - this.mPaddingBottom) / available;
            progress = this.mTouchProgressOffset;
        }
        return progress + (maxSteps * scale);
    }

    private float calculateProgressHorizontal(MotionEvent event, int maxSteps) {
        float scale;
        int width = this.mViewGroup.getWidth();
        int available = (width - this.mPaddingLeft) - this.mPaddingRight;
        int x = width - ((int) event.getX());
        float progress = 0.0f;
        if (x < this.mPaddingRight) {
            scale = 0.0f;
        } else if (x > width - this.mPaddingLeft) {
            scale = 1.0f;
        } else {
            scale = (x - this.mPaddingRight) / available;
            progress = this.mTouchProgressOffset;
        }
        return progress + (maxSteps * scale);
    }

    private Coverage getCoverageHorizontal(MotionEvent event) {
        int halfScreen = this.mViewGroup.getHeight() / 2;
        int y = (int) event.getY();
        return y <= halfScreen ? Coverage.LEFT : Coverage.RIGHT;
    }

    private Coverage getCoverageVertical(MotionEvent event) {
        int halfScreen = this.mViewGroup.getWidth() / 2;
        int x = (int) event.getX();
        return x <= halfScreen ? Coverage.LEFT : Coverage.RIGHT;
    }

    private void checkPlayerOverlaysView() {
        try {
            if ((this.mViewGroup.getHeight() == 0 || this.mViewGroup.getWidth() == 0) && SwipeHelper.nextGenWatchLayout != null) {
                View layout = SwipeHelper.nextGenWatchLayout.findViewById(getIdentifier());
                if (layout != null) {
                    this.mViewGroup = (ViewGroup) layout;
                    this.mBrightness.refreshViewGroup(this.mViewGroup, ReVancedSettingsFragment.overlayContext);
                    this.mVolume.refreshViewGroup(this.mViewGroup);
                    LogHelper.debug("Settings", "player_overlays refreshed");
                } else {
                    LogHelper.debug("Settings", "player_overlays was not found");
                }
            }
        } catch (Exception ex) {
            LogHelper.printException("XError", "Unable to refresh player_overlays layout", ex);
        }
    }

    private static int getIdentifier() {
        Context context = ReVancedUtils.getContext();
        assert context != null;
        return context.getResources().getIdentifier("player_overlays", "id", context.getPackageName());
    }
}
