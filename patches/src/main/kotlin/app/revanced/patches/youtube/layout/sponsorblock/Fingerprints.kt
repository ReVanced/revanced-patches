package app.revanced.patches.youtube.layout.sponsorblock

import app.revanced.patcher.InstructionLocation.*
import app.revanced.patcher.checkCast
import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import app.revanced.patches.youtube.shared.seekbarFingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionReversed
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val appendTimeFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Ljava/lang/CharSequence;", "Ljava/lang/CharSequence;", "Ljava/lang/CharSequence;")
    instructions(
        resourceLiteral(ResourceType.STRING, "total_time"),

        methodCall(smali = "Landroid/content/res/Resources;->getString(I[Ljava/lang/Object;)Ljava/lang/String;"),
        opcode(Opcode.MOVE_RESULT_OBJECT, MatchAfterImmediately())
    )
}

internal val controlsOverlayFingerprint = fingerprint {
    returns("V")
    parameters()
    instructions(
        resourceLiteral(ResourceType.ID, "inset_overlay_view_layout"),
        checkCast("Landroid/widget/FrameLayout;", MatchAfterWithin(20))
    )
}

/**
 * Resolves to the class found in [seekbarFingerprint].
 */
internal val rectangleFieldInvalidatorFingerprint = fingerprint {
    returns("V")
    parameters()
    instructions(
        methodCall(name = "invalidate")
    )
}

internal val adProgressTextViewVisibilityFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Z")
    custom { method, _ ->
        indexOfAdProgressTextViewVisibilityInstruction(method) >= 0
    }
}

internal fun indexOfAdProgressTextViewVisibilityInstruction(method: Method) =
    method.indexOfFirstInstructionReversed {
        val reference = getReference<MethodReference>()
        reference?.definingClass ==
                "Lcom/google/android/libraries/youtube/ads/player/ui/AdProgressTextView;"
                && reference.name =="setVisibility"
    }

