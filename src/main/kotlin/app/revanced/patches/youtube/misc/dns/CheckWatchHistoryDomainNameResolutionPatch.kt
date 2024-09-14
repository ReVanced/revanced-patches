package app.revanced.patches.youtube.misc.dns

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.shared.fingerprints.MainActivityOnCreateFingerprint
import app.revanced.util.resultOrThrow

@Patch(
    name = "Check watch history domain name resolution",
    description = "Checks if the device DNS server is preventing user watch history from being saved.",
    dependencies = [IntegrationsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39",
                "19.03.36",
                "19.04.38",
                "19.05.36",
                "19.06.39",
                "19.07.40",
                "19.08.36",
                "19.09.38",
                "19.10.39",
                "19.11.43",
                "19.12.41",
                "19.13.37",
                "19.14.43",
                "19.15.36",
                "19.16.39",
                "19.17.41",
                "19.18.41",
                "19.19.39",
                "19.20.35",
                "19.21.40",
                "19.22.43",
                "19.23.40",
                "19.24.45",
                "19.25.37", 
                "19.26.42",
                "19.28.42",
                "19.29.42",
                "19.30.39",
                "19.31.36",
                "19.32.36",
                "19.33.36",
                "19.34.42",
                "19.35.36",
                "19.36.37",
            ],
        ),
    ],
)
@Suppress("unused")
internal object CheckWatchHistoryDomainNameResolutionPatch : BytecodePatch(
    setOf(MainActivityOnCreateFingerprint),
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/CheckWatchHistoryDomainNameResolutionPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        MainActivityOnCreateFingerprint.resultOrThrow().mutableMethod.addInstructions(
            // FIXME: Insert index must be greater than the insert index used by GmsCoreSupport,
            //  as both patch the same method and GmsCoreSupport check should be first,
            //  but the patch does not depend on GmsCoreSupport, so it should not be possible to enforce this
            //  unless a third patch is added that this patch and GmsCoreSupport depend on to manage
            //  the order of the patches.
            1,
            "invoke-static/range { p0 .. p0 }, $INTEGRATIONS_CLASS_DESCRIPTOR->checkDnsResolver(Landroid/app/Activity;)V",
        )
    }
}
