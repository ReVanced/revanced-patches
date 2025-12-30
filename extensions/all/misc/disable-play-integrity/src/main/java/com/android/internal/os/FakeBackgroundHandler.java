package com.android.internal.os;

import android.os.Handler;
import android.os.Looper;

public class FakeBackgroundHandler {

    public static Handler getHandler() {
        return new Handler(Looper.getMainLooper());
    }
}
