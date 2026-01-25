package app.revanced.patches.youtube.misc.gms

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val specificNetworkErrorViewControllerMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes()
    instructions(
        ResourceType.DRAWABLE("ic_offline_no_content_upside_down"),
        ResourceType.STRING("offline_no_content_body_text_not_offline_eligible"),
        method { name == "getString" && returnType == "Ljava/lang/String;" },
        Opcode.MOVE_RESULT_OBJECT(),
    )
}

// It's not clear if this second class is ever used and it may be dead code,
// but it the layout image/text is identical to the network error fingerprint above.
internal val loadingFrameLayoutControllerMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L")
    instructions(
        ResourceType.DRAWABLE("ic_offline_no_content_upside_down"),
        ResourceType.STRING("offline_no_content_body_text_not_offline_eligible"),
        method { name == "getString" && returnType == "Ljava/lang/String;" },
        Opcode.MOVE_RESULT_OBJECT(),
    )
}
