package app.revanced.extension.youtube.videoplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.libraries.youtube.common.ui.TouchImageView;

import app.revanced.extension.shared.Logger;

public class HookedTouchImageView extends TouchImageView {
    public interface HookedTouchImageViewListener {
        void onVisibilityChanged(HookedTouchImageView view, int visibility);
    }

    @Nullable
    private HookedTouchImageViewListener listener;

    private int lastVisibility = View.VISIBLE;

    public HookedTouchImageView(Context context) {
        super(context);
    }

    public HookedTouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HookedTouchImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HookedTouchImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setVisibilityChangeListener(@NonNull HookedTouchImageViewListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        try {
            if (listener != null && lastVisibility != visibility) {
                lastVisibility = visibility;
                listener.onVisibilityChanged(this, visibility);
            }
        } catch (Exception ex) {
            Logger.printDebug(() -> "onVisibilityChanged failure", ex);
        }
    }
}
