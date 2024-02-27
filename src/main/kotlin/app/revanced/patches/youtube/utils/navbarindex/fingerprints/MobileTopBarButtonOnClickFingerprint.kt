package app.revanced.patches.youtube.utils.navbarindex.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object MobileTopBarButtonOnClickFingerprint : MethodFingerprint(
    strings = listOf("MenuButtonRendererKey"),
    customFingerprint = { methodDef, _ -> methodDef.name == "onClick" }
)