package app.revanced.patches.youtube.layout.shortsautoplay

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val reelEnumConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    opcodes(Opcode.RETURN_VOID)
    strings(
        "REEL_LOOP_BEHAVIOR_UNKNOWN",
        "REEL_LOOP_BEHAVIOR_SINGLE_PLAY",
        "REEL_LOOP_BEHAVIOR_REPEAT",
        "REEL_LOOP_BEHAVIOR_END_SCREEN",
    )
}

internal val reelPlaybackRepeatFingerprint = fingerprint {
    returns("V")
    parameters("L")
    strings("YoutubePlayerState is in throwing an Error.")
}

internal val reelPlaybackFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("J")
    custom { method, _ ->
        indexOfMilliSecondsInstruction(method) >= 0 &&
                indexOfInitializationInstruction(method) >= 0
    }
}

private fun indexOfMilliSecondsInstruction(method: Method) =
    method.indexOfFirstInstruction {
        getReference<FieldReference>()?.name == "MILLISECONDS"
    }

internal fun indexOfInitializationInstruction(method: Method) =
    method.indexOfFirstInstruction {
        val reference = getReference<MethodReference>()
        opcode == Opcode.INVOKE_DIRECT &&
                reference?.name == "<init>" &&
                reference.parameterTypes.size == 3 &&
                reference.parameterTypes.firstOrNull() == "I"
    }
