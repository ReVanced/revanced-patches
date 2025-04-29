package app.revanced.patches.youtube.misc.gms

import app.revanced.patcher.fingerprint
import app.revanced.util.containsLiteralInstruction
import com.android.tools.smali.dexlib2.AccessFlags

internal val specificNetworkErrorViewControllerFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    custom { method, _ ->
        method.containsLiteralInstruction(ic_offline_no_content_upside_down)
                && method.containsLiteralInstruction(offline_no_content_body_text_not_offline_eligible)
    }
}

// It's not clear if this second class is ever used and it may be dead code,
// but it the layout image/text is identical to the network error fingerprint above.
internal val loadingFrameLayoutControllerFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    custom { method, _ ->
        method.containsLiteralInstruction(ic_offline_no_content_upside_down)
                && method.containsLiteralInstruction(offline_no_content_body_text_not_offline_eligible)
    }
}