package app.revanced.patches.youtube.misc.engagementpanel

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val engagementPanelBuilderFingerprint = fingerprint {
    returns("L")
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    parameters("L", "L", "Z", "Z")
    strings(
        "EngagementPanelController: cannot show EngagementPanel before EngagementPanelController.init() has been called.",
        "[EngagementPanel] Cannot show EngagementPanel before EngagementPanelController.init() has been called."
    )
}

internal val engagementPanelLayoutFingerprint = fingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("L", "L", "I")
    custom { method: Method, _ ->
        method.indexOfFirstInstruction {
            opcode == Opcode.INVOKE_VIRTUAL &&
                    getReference<MethodReference>()?.toString() == "Landroid/widget/FrameLayout;->indexOfChild(Landroid/view/View;)I"
        } >= 0
    }
}

internal val engagementPanelUpdateFingerprint = fingerprint {
    returns("V")
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    parameters("L", "Z")
    custom { method: Method, _ ->
        method.indexOfFirstInstruction {
            opcode == Opcode.INVOKE_VIRTUAL &&
                    getReference<MethodReference>()?.toString() == "Ljava/util/ArrayDeque;->pop()Ljava/lang/Object;"
        } >= 0
    }
}