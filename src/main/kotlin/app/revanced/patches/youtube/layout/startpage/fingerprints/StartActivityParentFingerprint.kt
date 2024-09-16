package app.revanced.patches.youtube.layout.startpage.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.util.containsWideLiteralInstructionValue

internal object StartActivityParentFingerprint : MethodFingerprint(
    returnType = "Z",
    parameters = listOf(),
    customFingerprint = { method, _ ->
        method.name == "isInMultiWindowMode" && method.containsWideLiteralInstructionValue(45372462L)
    }
)