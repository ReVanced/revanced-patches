package app.revanced.integrations.videoswipecontrols;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import app.revanced.integrations.utils.LogHelper;

/* loaded from: classes6.dex */
public class SwipeGestureListener implements GestureDetector.OnGestureListener {
    private boolean ignoreScroll = false;
    private final SwipeEventsListener listener;
    private final int minFlingVelocity;
    public static int SWIPE_THRESHOLD = 0;
    public static int TOP_PADDING = 20;

    public SwipeGestureListener(SwipeEventsListener listener, ViewConfiguration viewConfiguration) {
        this.listener = listener;
        this.minFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onSingleTapUp(MotionEvent e) {
        this.listener.onTap();
        return false;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public void onLongPress(MotionEvent e) {
        LogHelper.debug(SwipeGestureListener.class, "Long Press");
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        LogHelper.debug(SwipeGestureListener.class, "Scroll");
        if (e1 == null || e2 == null) {
            if (e1 == null) {
                LogHelper.debug(SwipeGestureListener.class, "e1 is null");
            }
            if (e2 == null) {
                LogHelper.debug(SwipeGestureListener.class, "e2 is null");
            }
            return false;
        } else if (this.ignoreScroll) {
            LogHelper.debug(SwipeGestureListener.class, "Scroll ignored");
            return false;
        } else {
            float deltaY = e2.getY() - e1.getY();
            float deltaX = e2.getX() - e1.getX();
            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                if (Math.abs(deltaX) > SWIPE_THRESHOLD) {
                    this.listener.onHorizontalScroll(e2, deltaX);
                    String message = deltaX > 0.0f ? "Slide right" : "Slide left";
                    LogHelper.debug(SwipeGestureListener.class, message);
                }
            } else if (Math.abs(deltaY) > SWIPE_THRESHOLD) {
                this.listener.onVerticalScroll(e2, deltaY);
                String message = deltaY > 0.0f ? "Slide down" : "Slide up";
                LogHelper.debug(SwipeGestureListener.class, message);
            }
            return false;
        }
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        LogHelper.debug(SwipeGestureListener.class, "Fling");
        try {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > this.minFlingVelocity) {
                    if (diffX > 0.0f) {
                        this.listener.onSwipeRight();
                    } else {
                        this.listener.onSwipeLeft();
                    }
                }
            } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > this.minFlingVelocity) {
                if (diffY > 0.0f) {
                    this.listener.onSwipeBottom();
                } else {
                    this.listener.onSwipeTop();
                }
            }
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public void onShowPress(MotionEvent e) {
        LogHelper.debug(SwipeGestureListener.class, "Show Press");
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onDown(MotionEvent e) {
        LogHelper.debug(SwipeGestureListener.class, "Down - x: " + e.getX() + " y: " + e.getY());
        this.ignoreScroll = e.getY() <= TOP_PADDING;
        this.listener.onDown(e);
        return false;
    }
}
