package fi.razerman.youtube.Fenster;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import fi.razerman.youtube.XGlobals;

/* loaded from: classes6.dex */
public class FensterGestureListener implements GestureDetector.OnGestureListener {
    public static final String TAG = "FensterGestureListener";
    private boolean ignoreScroll = false;
    private final FensterEventsListener listener;
    private final int minFlingVelocity;
    public static int SWIPE_THRESHOLD = 0;
    public static int TOP_PADDING = 20;

    public FensterGestureListener(FensterEventsListener listener, ViewConfiguration viewConfiguration) {
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
        if (XGlobals.debug) {
            Log.i(TAG, "Long Press");
        }
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (XGlobals.debug) {
            Log.i(TAG, "Scroll");
        }
        if (e1 == null || e2 == null) {
            if (e1 == null && XGlobals.debug) {
                Log.d("XDebug", "e1 is null");
            }
            if (e2 == null && XGlobals.debug) {
                Log.d("XDebug", "e2 is null");
            }
            return false;
        } else if (this.ignoreScroll) {
            if (XGlobals.debug) {
                Log.i(TAG, "Scroll ignored");
            }
            return false;
        } else {
            float deltaY = e2.getY() - e1.getY();
            float deltaX = e2.getX() - e1.getX();
            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                if (Math.abs(deltaX) > SWIPE_THRESHOLD) {
                    this.listener.onHorizontalScroll(e2, deltaX);
                    if (deltaX > 0.0f) {
                        if (XGlobals.debug) {
                            Log.i(TAG, "Slide right");
                        }
                    } else if (XGlobals.debug) {
                        Log.i(TAG, "Slide left");
                    }
                }
            } else if (Math.abs(deltaY) > SWIPE_THRESHOLD) {
                this.listener.onVerticalScroll(e2, deltaY);
                if (deltaY > 0.0f) {
                    if (XGlobals.debug) {
                        Log.i(TAG, "Slide down");
                    }
                } else if (XGlobals.debug) {
                    Log.i(TAG, "Slide up");
                }
            }
            return false;
        }
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (XGlobals.debug) {
            Log.i(TAG, "Fling");
        }
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
        if (XGlobals.debug) {
            Log.i(TAG, "Show Press");
        }
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onDown(MotionEvent e) {
        if (XGlobals.debug) {
            Log.i(TAG, "Down - x: " + e.getX() + " y: " + e.getY());
        }
        this.ignoreScroll = e.getY() <= TOP_PADDING;
        this.listener.onDown(e);
        return false;
    }
}
