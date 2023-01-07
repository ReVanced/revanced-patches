package app.revanced.integrations.patches.playback.speed;

public class CustomVideoSpeedPatch {
    // Values are useless as they are being overridden by the respective patch.
    // This generates a .array segment in Dalvik bytecode
    // which the patch utilizes to store the video speeds in, only
    // if it has two or more default values.
    public static final float[] videoSpeeds = { 0, 0 };
}
