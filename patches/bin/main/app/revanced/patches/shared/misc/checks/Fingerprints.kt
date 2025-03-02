package app.revanced.patches.shared.misc.checks

import app.revanced.patcher.fingerprint

internal val patchInfoFingerprint = fingerprint {
    custom { _, classDef -> classDef.type == "Lapp/revanced/extension/shared/checks/PatchInfo;" }
}

internal val patchInfoBuildFingerprint = fingerprint {
    custom { _, classDef -> classDef.type == "Lapp/revanced/extension/shared/checks/PatchInfo\$Build;" }
}
