package app.revanced.integrations.videoswipecontrols.seekbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import app.revanced.integrations.utils.LogHelper;

/* loaded from: classes6.dex */
public class VolumeSeekBar {
    public int Max;
    public int Progress;
    private AudioManager audioManager;
    private boolean enabled;
    Handler handler;
    private boolean isRegistered;
    private Context mContext;
    TextView mTextView;
    ViewGroup mViewGroup;
    private final BroadcastReceiver volumeReceiver = new BroadcastReceiver() { // from class: app.revanced.integrations.videoplayer.Fenster.Seekbar.VolumeSeekBar.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            VolumeSeekBar.this.updateVolumeProgress();
            VolumeSeekBar.this.hideDelayed();
        }
    };

    public void initialise(Context context, ViewGroup viewGroup) {
        this.enabled = false;
        this.mViewGroup = viewGroup;
        this.mContext = context;
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.Max = this.audioManager.getStreamMaxVolume(3);
        this.Progress = this.audioManager.getStreamVolume(3);
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
        this.mViewGroup = viewGroup;
        this.mViewGroup.addView(this.mTextView);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateVolumeProgress() {
        this.Progress = this.audioManager.getStreamVolume(3);
        if (this.mTextView != null) {
            this.mTextView.setText("Volume: " + this.Progress);
            if (!isVisible()) {
                this.mTextView.setVisibility(View.VISIBLE);
            }
        }
        LogHelper.debug(VolumeSeekBar.class, "updateVolumeProgress: " + this.Progress);
    }

    private void setVolume(int volume) {
        this.audioManager.setStreamVolume(3, volume, 0);
        updateVolumeProgress();
    }

    private void registerVolumeReceiver() {
        this.mContext.registerReceiver(this.volumeReceiver, new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));
        this.isRegistered = true;
    }

    public void unregisterVolumeReceiver() {
        this.mContext.unregisterReceiver(this.volumeReceiver);
        this.isRegistered = false;
    }

    public void manuallyUpdate(int update) {
        if (this.enabled) {
            setVolume(update);
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
        // from class: app.revanced.integrations.videoplayer.Fenster.Seekbar.VolumeSeekBar.2
// java.lang.Runnable
        this.handler.postDelayed(VolumeSeekBar.this::hide, 2000L);
    }

    public boolean isVisible() {
        return this.mTextView != null && this.mTextView.getVisibility() == View.VISIBLE;
    }

    public void disable() {
        this.enabled = false;
        if (this.isRegistered) {
            unregisterVolumeReceiver();
        }
    }

    public void enable() {
        this.enabled = true;
        this.Progress = this.audioManager.getStreamVolume(3);
        if (!this.isRegistered) {
            registerVolumeReceiver();
        }
    }
}
