package app.revanced.patches.youtube.misc.gms

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patches.shared.fingerprints.HomeActivityFingerprint
import app.revanced.patches.shared.misc.gms.AbstractGmsCoreSupportPatch
import app.revanced.patches.youtube.layout.buttons.cast.HideCastButtonPatch
import app.revanced.patches.youtube.misc.fix.playback.ClientSpoofPatch
import app.revanced.patches.youtube.misc.gms.Constants.REVANCED_YOUTUBE_PACKAGE_NAME
import app.revanced.patches.youtube.misc.gms.Constants.YOUTUBE_PACKAGE_NAME
import app.revanced.patches.youtube.misc.gms.GmsCoreSupportResourcePatch.gmsCoreVendorOption
import app.revanced.patches.youtube.misc.gms.fingerprints.*
import app.revanced.util.exception


@Suppress("unused")
object GmsCoreSupportPatch : AbstractGmsCoreSupportPatch(
    fromPackageName = YOUTUBE_PACKAGE_NAME,
    toPackageName = REVANCED_YOUTUBE_PACKAGE_NAME,
    primeMethodFingerprint = PrimeMethodFingerprint,
    earlyReturnFingerprints = setOf(
        ServiceCheckFingerprint,
        GooglePlayUtilityFingerprint,
        CastDynamiteModuleFingerprint,
        CastDynamiteModuleV2Fingerprint,
        CastContextFetchFingerprint
    ),
    dependencies = setOf(
        HideCastButtonPatch::class,
        ClientSpoofPatch::class
    ),
    abstractGmsCoreSupportResourcePatch = GmsCoreSupportResourcePatch,
    compatiblePackages = setOf(
        CompatiblePackage(
            "com.google.android.youtube", setOf(
                "18.48.39",
                "18.49.37",
                "19.01.34"
            )
        )
    ),
    fingerprints = setOf(
        ServiceCheckFingerprint,
        GooglePlayUtilityFingerprint,
        CastDynamiteModuleFingerprint,
        CastDynamiteModuleV2Fingerprint,
        CastContextFetchFingerprint,
        PrimeMethodFingerprint,
        HomeActivityFingerprint,
    )
) {
    override val gmsCoreVendor by gmsCoreVendorOption

    override fun execute(context: BytecodeContext) {
        // Check the availability of GmsCore.
        HomeActivityFingerprint.result?.mutableMethod?.addInstruction(
            0,
            "invoke-static {}, Lapp/revanced/integrations/youtube/patches/GmsCoreSupport;->checkAvailability()V"
        ) ?: throw HomeActivityFingerprint.exception

        super.execute(context)
    }
}
