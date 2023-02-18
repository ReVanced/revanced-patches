package app.revanced.patches.twelvewidgets.unlock.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

abstract class MethodUnlockFingerprint(private val className: String) : MethodFingerprint(
    "L",
    strings = listOf("binding.addButton"),
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("/$className;")
    }
)

