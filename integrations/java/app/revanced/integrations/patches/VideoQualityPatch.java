package app.revanced.integrations.patches;

import app.revanced.integrations.videoplayer.videosettings.VideoQuality;

public class VideoQualityPatch {

    //ToDo: Write Patch for it.
    //See https://drive.google.com/file/d/1goodaU0JWrO9BAOUn6El-Id1SNuMGHR9/view?usp=sharing for where it needs to be used.
    public static int setVideoQuality(Object[] qualities, int quality, Object qInterface) {
        return VideoQuality.setVideoQuality(qualities, quality, qInterface);
    }

    //See https://drive.google.com/file/d/1_cgCf603XKk4gEbbsmWGtndNt5UJ0np7/view?usp=sharing for usage
    public static void userChangedQuality() {
        VideoQuality.userChangedQuality();
    }
}
