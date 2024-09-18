package app.revanced.patches.shared.misc.checks.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object PatchInfoFingerprint : MethodFingerprint(
    customFingerprint = { _, classDef ->
        classDef.type == "Lapp/revanced/integrations/shared/checks/PatchInfo;"
    },
)
