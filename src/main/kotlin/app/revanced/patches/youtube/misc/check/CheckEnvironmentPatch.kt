package app.revanced.patches.youtube.misc.check

import app.revanced.patches.shared.misc.checks.BaseCheckEnvironmentPatch
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.shared.fingerprints.MainActivityOnCreateFingerprint

@Suppress("unused")
object CheckEnvironmentPatch :
    BaseCheckEnvironmentPatch(
        mainActivityOnCreateFingerprint = MainActivityOnCreateFingerprint,
        integrationsPatch = IntegrationsPatch,
        compatiblePackages = setOf(CompatiblePackage("com.google.android.youtube")),
    )
