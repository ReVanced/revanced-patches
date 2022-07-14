package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class VideoBufferPatch {

    public static int getMaxBuffer() {
        int confVal = SettingsEnum.MAX_BUFFER.getInt();
        if (confVal < 1) confVal = 1;
        return confVal;
    }

    public static int getPlaybackBuffer() {
        int confVal = SettingsEnum.PLAYBACK_MAX_BUFFER.getInt();
        if (confVal < 1) confVal = 1;
        return confVal;
    }

    public static int getReBuffer() {
        int confVal = SettingsEnum.MAX_PLAYBACK_BUFFER_AFTER_REBUFFER.getInt();
        if (confVal < 1) confVal = 1;
        return confVal;
    }


}
