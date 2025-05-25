package app.revanced.patches.youtube.misc.hapticfeedback

import com.android.tools.smali.dexlib2.Opcode
import app.revanced.patcher.fingerprint

internal val seekHapticsFingerprint = fingerprint {
    returns("V")
    opcodes(Opcode.SGET)
    strings("Failed to easy seek haptics vibrate.")
    custom { method, _ -> method.name == "run" }
}

internal val markerHapticsFingerprint = fingerprint {
    returns("V")
    strings("Failed to execute markers haptics vibrate.")
}

internal val scrubbingHapticsFingerprint = fingerprint {
    returns("V")
    strings("Failed to haptics vibrate for fine scrubbing.")
}

internal val seekUndoHapticsFingerprint = fingerprint {
    returns("V")
    strings("Failed to execute seek undo haptics vibrate.")
}

internal val zoomHapticsFingerprint = fingerprint {
    returns("V")
    strings("Failed to haptics vibrate for video zoom")
}
