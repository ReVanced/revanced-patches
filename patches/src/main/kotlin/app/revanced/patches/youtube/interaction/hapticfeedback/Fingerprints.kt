package app.revanced.patches.youtube.interaction.hapticfeedback

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

internal val BytecodePatchContext.markerHapticsMethod by gettingFirstMethodDeclaratively(
    "Failed to execute markers haptics vibrate.",
) {
    returnType("V")
}

internal val BytecodePatchContext.scrubbingHapticsMethod by gettingFirstMethodDeclaratively(
    "Failed to haptics vibrate for fine scrubbing.",
) {
    returnType("V")
}

internal val BytecodePatchContext.seekUndoHapticsMethod by gettingFirstMethodDeclaratively(
    "Failed to execute seek undo haptics vibrate.",
) {
    returnType("V")
}

internal val BytecodePatchContext.tapAndHoldHapticsHandlerMethodMatch by composingFirstMethod {
    name("<init>")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returnType("V")
    parameterTypes("Landroid/content/Context;", "Landroid/os/Handler;")
    instructions(
        "vibrator"(),
        allOf(Opcode.CHECK_CAST(), type("Landroid/os/Vibrator;")),
        after(allOf(Opcode.IPUT_OBJECT(), field { type == "Ljava/lang/Object;" }))
    )
}

internal fun BytecodePatchContext.getTapAndHoldHapticsMethodMatch(vibratorFieldReference: FieldReference) =
    firstMethodComposite {
        name("run")
        accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
        returnType("V")
        parameterTypes()
        instructions(
            allOf(Opcode.IGET_OBJECT(), field { this == vibratorFieldReference }),
            allOf(Opcode.CHECK_CAST(), type("Landroid/os/Vibrator;")),
            "Failed to easy seek haptics vibrate."()
        )
    }

internal val BytecodePatchContext.zoomHapticsMethod by gettingFirstMethodDeclaratively(
    "Failed to haptics vibrate for video zoom",
) {
    returnType("V")
}
