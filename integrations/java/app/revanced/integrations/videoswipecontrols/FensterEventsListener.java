package app.revanced.integrations.videoswipecontrols;

import android.view.MotionEvent;

/* loaded from: classes6.dex */
public interface FensterEventsListener {
    void onDown(MotionEvent motionEvent);

    void onHorizontalScroll(MotionEvent motionEvent, float f);

    void onSwipeBottom();

    void onSwipeLeft();

    void onSwipeRight();

    void onSwipeTop();

    void onTap();

    void onUp();

    void onVerticalScroll(MotionEvent motionEvent, float f);
}
