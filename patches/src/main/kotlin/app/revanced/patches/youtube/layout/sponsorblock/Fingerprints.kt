package app.revanced.patches.youtube.layout.sponsorblock

import app.revanced.patcher.checkCast
import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import app.revanced.patches.youtube.shared.seekbarFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val appendTimeFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Ljava/lang/CharSequence;", "Ljava/lang/CharSequence;", "Ljava/lang/CharSequence;")
    instructions(
        resourceLiteral(ResourceType.STRING, "total_time"),

        methodCall(smali = "Landroid/content/res/Resources;->getString(I[Ljava/lang/Object;)Ljava/lang/String;"),
        opcode(Opcode.MOVE_RESULT_OBJECT, maxAfter = 0)
    )
}

internal val controlsOverlayFingerprint by fingerprint {
    returns("V")
    parameters()
    instructions(
        resourceLiteral(ResourceType.ID, "inset_overlay_view_layout"),
        checkCast("Landroid/widget/FrameLayout;", maxAfter = 20)
    )
}

/**
 * Resolves to the class found in [seekbarFingerprint].
 */
internal val rectangleFieldInvalidatorFingerprint by fingerprint {
    returns("V")
    parameters()
    instructions(
        methodCall(name = "invalidate")
    )
}
