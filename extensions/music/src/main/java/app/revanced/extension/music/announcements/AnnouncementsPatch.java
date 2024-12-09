package app.revanced.extension.music.announcements;

import android.app.Activity;
import android.os.Build;
import androidx.annotation.RequiresApi;
import app.revanced.extension.shared.announcements.BaseAnnouncementsPatch;

@SuppressWarnings("unused")
public class AnnouncementsPatch extends BaseAnnouncementsPatch {
    private static final AnnouncementsPatch INSTANCE = new AnnouncementsPatch();

    private AnnouncementsPatch() {
        super("music");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void showAnnouncement(final Activity context) {
        INSTANCE._showAnnouncement(context);
    }
}
