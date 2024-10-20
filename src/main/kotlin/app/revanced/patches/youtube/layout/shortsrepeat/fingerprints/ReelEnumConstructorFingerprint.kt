package app.revanced.patches.youtube.layout.shortsrepeat.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import kotlin.collections.listOf

internal object ReelEnumConstructorFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.STATIC or AccessFlags.CONSTRUCTOR,
    returnType = "V",
    strings = listOf(
        "REEL_LOOP_BEHAVIOR_UNKNOWN",
        "REEL_LOOP_BEHAVIOR_SINGLE_PLAY",
        "REEL_LOOP_BEHAVIOR_REPEAT",
        "REEL_LOOP_BEHAVIOR_END_SCREEN"
    )
)
