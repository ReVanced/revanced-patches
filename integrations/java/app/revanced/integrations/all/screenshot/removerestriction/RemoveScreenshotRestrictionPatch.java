package app.revanced.integrations.all.screenshot.removerestriction;

import android.view.Window;
import android.view.WindowManager;

public class RemoveScreenshotRestrictionPatch {

    public static void addFlags(Window window, int flags) {
        window.addFlags(flags & ~WindowManager.LayoutParams.FLAG_SECURE);
    }

    public static void setFlags(Window window, int flags, int mask) {
        window.setFlags(flags & ~WindowManager.LayoutParams.FLAG_SECURE, mask & ~WindowManager.LayoutParams.FLAG_SECURE);
    }
}
