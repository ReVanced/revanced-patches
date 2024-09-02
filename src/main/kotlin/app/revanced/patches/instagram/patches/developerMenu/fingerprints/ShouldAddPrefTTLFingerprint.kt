package app.revanced.patches.instagram.patches.developerMenu.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object ShouldAddPrefTTLFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.name == "shouldAddPrefTTL" && methodDef.definingClass == "Lcom/instagram/debug/whoptions/WhitehatOptionsFragment;"
    },
)
