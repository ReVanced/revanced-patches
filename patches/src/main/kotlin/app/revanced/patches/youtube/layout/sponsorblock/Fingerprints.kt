package app.revanced.patches.youtube.layout.sponsorblock

import app.revanced.patcher.checkCast
import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import app.revanced.patches.youtube.shared.seekbarFingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionReversed
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val appendTimeFingerprint by fingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("Ljava/lang/CharSequence;", "Ljava/lang/CharSequence;", "Ljava/lang/CharSequence;")
    instructions(
        resourceLiteral("string", "total_time"),

        methodCall(smali = "Landroid/content/res/Resources;->getString(I[Ljava/lang/Object;)Ljava/lang/String;"),
        opcode(Opcode.MOVE_RESULT_OBJECT, maxAfter = 0)
    )
}

internal val controlsOverlayFingerprint by fingerprint {
    returns("V")
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    parameters()
    instructions(
        resourceLiteral("id", "inset_overlay_view_layout"),
        checkCast("Landroid/widget/FrameLayout;", maxAfter = 20)
    )
}

/**
 * Resolves to the class found in [seekbarFingerprint].
 */
internal val rectangleFieldInvalidatorFingerprint by fingerprint {
    returns("V")
    parameters()
    custom  { method, _ ->
        indexOfInvalidateInstruction(method) >= 0
    }
}

internal fun indexOfInvalidateInstruction(method: Method) =
    method.indexOfFirstInstructionReversed {
        getReference<MethodReference>()?.name == "invalidate"
    }
