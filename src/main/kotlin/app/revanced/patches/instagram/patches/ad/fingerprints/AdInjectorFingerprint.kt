package app.revanced.patches.instagram.patches.ad.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object AdInjectorFingerprint : MethodFingerprint(
    "Z",
    AccessFlags.PRIVATE.value,
    listOf("L", "L"),
    opcodes = listOf(
        Opcode.IGET,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
    ),
    strings = listOf(
        "SponsoredContentController::Delivery"
    )
)
