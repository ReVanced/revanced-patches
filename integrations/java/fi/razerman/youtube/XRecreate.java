package fi.razerman.youtube;

import android.app.Activity;

/* loaded from: classes6.dex */
final class XRecreate implements Runnable {
    private final Activity activity;

    /* JADX INFO: Access modifiers changed from: package-private */
    public XRecreate(Activity activity) {
        this.activity = activity;
    }

    @Override // java.lang.Runnable
    public final void run() {
        this.activity.recreate();
    }
}
