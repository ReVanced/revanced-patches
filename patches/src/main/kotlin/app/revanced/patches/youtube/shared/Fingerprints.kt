package app.revanced.patches.youtube.shared

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val autoRepeatFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    custom { method, _ ->
        method.implementation!!.instructions.count() == 3 && method.annotations.isEmpty()
    }
}

internal val autoRepeatParentFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    strings(
        "play() called when the player wasn't loaded.",
        "play() blocked because Background Playability failed",
    )
}

internal val homeActivityFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "onCreate" && classDef.endsWith("Shell_HomeActivity;")
    }
}

internal val layoutConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    strings("1.0x")
}

internal val mainActivityFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters()
    custom { _, classDef ->
        // Old versions of YouTube called this class "WatchWhileActivity" instead.
        classDef.endsWith("MainActivity;") || classDef.endsWith("WatchWhileActivity;")
    }
}

internal val mainActivityOnCreateFingerprint = fingerprint {
    returns("V")
    parameters("Landroid/os/Bundle;")
    custom { method, classDef ->
        method.name == "onCreate" &&
            (
                classDef.endsWith("MainActivity;") ||
                    // Old versions of YouTube called this class "WatchWhileActivity" instead.
                    classDef.endsWith("WatchWhileActivity;")
                )
    }
}

val rollingNumberTextViewAnimationUpdateFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/graphics/Bitmap;")
    opcodes(
        Opcode.NEW_INSTANCE, // bitmap ImageSpan
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_4,
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.CONST_16,
        Opcode.INVOKE_VIRTUAL,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.INT_TO_FLOAT,
        Opcode.INVOKE_VIRTUAL, // set textview padding using bitmap width
    )
    custom { _, classDef ->
        classDef.superclass == "Landroid/support/v7/widget/AppCompatTextView;"
    }
}

internal val seekbarFingerprint = fingerprint {
    returns("V")
    strings("timed_markers_width")
}

internal val seekbarOnDrawFingerprint = fingerprint {
    custom { method, _ -> method.name == "onDraw" }
}
