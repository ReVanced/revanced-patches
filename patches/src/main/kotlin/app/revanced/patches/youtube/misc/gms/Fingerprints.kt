package app.revanced.patches.youtube.misc.gms

import app.revanced.patcher.InstructionLocation.*
import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val specificNetworkErrorViewControllerFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    instructions(
        resourceLiteral(ResourceType.DRAWABLE, "ic_offline_no_content_upside_down"),
        resourceLiteral(ResourceType.STRING, "offline_no_content_body_text_not_offline_eligible"),
        methodCall(name = "getString", returnType = "Ljava/lang/String;"),
        opcode(Opcode.MOVE_RESULT_OBJECT, location = MatchAfterImmediately())
    )
}

// It's not clear if this second class is ever used and it may be dead code,
// but it the layout image/text is identical to the network error fingerprint above.
internal val loadingFrameLayoutControllerFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    instructions(
        resourceLiteral(ResourceType.DRAWABLE, "ic_offline_no_content_upside_down"),
        resourceLiteral(ResourceType.STRING, "offline_no_content_body_text_not_offline_eligible"),
        methodCall(name = "getString", returnType = "Ljava/lang/String;"),
        opcode(Opcode.MOVE_RESULT_OBJECT, MatchAfterImmediately())
    )
}