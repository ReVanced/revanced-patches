package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class VideoBufferPatch {

    //ToDo: Write Patch for it.
    //See https://drive.google.com/file/d/1LSZZX4NgBIlN0dDCzyI7cECtgbXVg_1j/view?usp=sharing for where it needs to be used.
    public static int getMaxBuffer(int original) {
        return SettingsEnum.MAX_BUFFER_INTEGER.getInt();
    }

    //ToDo: Write Patch for it.
    //See https://drive.google.com/file/d/1gIUqPIMq-XP-edT_9wQN1RbmVnk9tJN8/view?usp=sharing for where it needs to be used.
    public static int getPlaybackBuffer(int original) {
        return SettingsEnum.PLAYBACK_MAX_BUFFER_INTEGER.getInt();
    }

    //ToDo: Write Patch for it.
    //See https://drive.google.com/file/d/1ywL7SxvWrBIIbuZ1YoUIKdZM-U8H_w-p/view?usp=sharing for where it needs to be used.
    public static int getReBuffer(int original) {
        return SettingsEnum.MAX_PLAYBACK_BUFFER_AFTER_REBUFFER_INTEGER.getInt();
    }


}
