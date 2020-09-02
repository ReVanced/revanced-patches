package com.google.android.apps.youtube.app;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;

public class YouTubeApplication extends Application {
    protected void onCreate(final Bundle bundle) {
        super.onCreate();
    }

    public static Context getAppContext() {
        return null;
    }
}
