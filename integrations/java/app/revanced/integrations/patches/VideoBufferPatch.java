package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class VideoBufferPatch {

    public static int getMaxBuffer() {
        return SettingsEnum.MAX_BUFFER_INTEGER.getInt();
    }

    public static int getPlaybackBuffer() {
        return SettingsEnum.PLAYBACK_MAX_BUFFER_INTEGER.getInt();
    }

    public static int getReBuffer() {
        return SettingsEnum.MAX_PLAYBACK_BUFFER_AFTER_REBUFFER_INTEGER.getInt();
    }


}
