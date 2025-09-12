package app.revanced.patches.shared.misc.checks

import app.revanced.patcher.fingerprint

internal val patchInfoFingerprint by fingerprint {
    custom { _, classDef -> classDef.type == "Lapp/revanced/extension/shared/checks/PatchInfo;" }
}

internal val patchInfoBuildFingerprint by fingerprint {
    custom { _, classDef -> classDef.type == "Lapp/revanced/extension/shared/checks/PatchInfo\$Build;" }
}
