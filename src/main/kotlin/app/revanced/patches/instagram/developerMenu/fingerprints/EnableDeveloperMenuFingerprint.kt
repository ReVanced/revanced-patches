package app.revanced.patches.instagram.developerMenu.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object EnableDeveloperMenuFingerprint:MethodFingerprint(
    customFingerprint = {methodDef, _ ->
        methodDef.name == "shouldAddPrefTTL" && methodDef.definingClass == "Lcom/instagram/debug/whoptions/WhitehatOptionsFragment;"}
)