package app.revanced.patches.youtube.misc.microg

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.fingerprints.WatchWhileActivityFingerprint
import app.revanced.patches.youtube.layout.buttons.cast.HideCastButtonPatch
import app.revanced.patches.youtube.misc.fix.playback.ClientSpoofPatch
import app.revanced.patches.youtube.misc.microg.fingerprints.*
import app.revanced.patches.youtube.misc.microg.shared.Constants.PACKAGE_NAME
import app.revanced.patches.youtube.misc.microg.shared.Constants.REVANCED_PACKAGE_NAME
import app.revanced.util.microg.MicroGBytecodeHelper

@Patch(
    name = "Vanced MicroG support",
    description = "Allows YouTube ReVanced to run without root and under a different package name with Vanced MicroG.",
    dependencies = [
        MicroGResourcePatch::class,
        HideCastButtonPatch::class,
        ClientSpoofPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.16.37",
                "18.19.35",
                "18.20.39",
                "18.23.35",
                "18.29.38",
                "18.32.39"
            ]
        )
    ]
)
@Suppress("unused")
object MicroGBytecodePatch : BytecodePatch(
    setOf(
        ServiceCheckFingerprint,
        GooglePlayUtilityFingerprint,
        CastDynamiteModuleFingerprint,
        CastDynamiteModuleV2Fingerprint,
        CastContextFetchFingerprint,
        PrimeFingerprint,
        WatchWhileActivityFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        // apply common microG patch
        MicroGBytecodeHelper.patchBytecode(
            context, arrayOf(
                MicroGBytecodeHelper.packageNameTransform(
                    PACKAGE_NAME,
                    REVANCED_PACKAGE_NAME
                )
            ),
            MicroGBytecodeHelper.PrimeMethodTransformationData(
                PrimeFingerprint,
                PACKAGE_NAME,
                REVANCED_PACKAGE_NAME
            ),
            listOf(
                ServiceCheckFingerprint,
                GooglePlayUtilityFingerprint,
                CastDynamiteModuleFingerprint,
                CastDynamiteModuleV2Fingerprint,
                CastContextFetchFingerprint
            )
        )

        // inject the notice for MicroG
        MicroGBytecodeHelper.injectNotice(WatchWhileActivityFingerprint)
    }
}
