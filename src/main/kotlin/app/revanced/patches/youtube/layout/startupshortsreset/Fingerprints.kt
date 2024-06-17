package app.revanced.patches.youtube.layout.startupshortsreset

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val userWasInShortsFingerprint = fingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("Ljava/lang/Object;")
    strings("Failed to read user_was_in_shorts proto after successful warmup")
}
