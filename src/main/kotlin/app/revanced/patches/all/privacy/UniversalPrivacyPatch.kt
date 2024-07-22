package app.revanced.patches.all.privacy

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.booleanPatchOption
import app.revanced.patches.all.privacy.fingerprints.*
import app.revanced.util.resultOrThrow
import java.util.logging.Logger

@Patch(
    name = "Disable privacy invasive components",
    description = "Disables multiple embedded analytics and telemetry SDKs",
)
@Suppress("unused")
object UniversalPrivacyPatch : BytecodePatch(
    setOf(
        StatsigClientFingerprint,
        AnalyticsInitFingerprint,
        AppsFlyerInitFingerprint,
        ComScoreSetupFingerprint,
        SettingsSpiCallFingerprint,
        DoConfigFetchFingerprint,
        SendFingerprint,
        MoEngageInitFingerprint,
        SegmentBuilderFingerprint
    )
) {

    private val subPatchesOptions = mapOf(
        ::disableGoogleAnalytics to booleanPatchOption(
            key = "disableGoogleAnalytics",
            default = true,
            values = mapOf(),
            title = "Google Analytics",
            description = "",
            required = true
        ),
        ::disableStatsig to booleanPatchOption(
            key = "disableStatsig",
            default = true,
            values = mapOf(),
            title = "Statsig",
            description = "",
            required = true
        ),
        ::disableAppsFlyer to booleanPatchOption(
            key = "disableAppsFlyer",
            default = true,
            values = mapOf(),
            title = "Apps Flyer",
            description = "",
            required = true
        ),
        ::disableComScore to booleanPatchOption(
            key = "disableComScore",
            default = true,
            values = mapOf(),
            title = "ComScore",
            description = "",
            required = true
        ),
        ::disableCrashlytics to booleanPatchOption(
            key = "disableCrashlytics",
            default = true,
            values = mapOf(),
            title = "Crashlytics",
            description = "",
            required = true
        ),
        ::disableFirebaseTransport to booleanPatchOption(
            key = "disableFirebaseTransport",
            default = true,
            values = mapOf(),
            title = "Firebase Transport",
            description = "",
            required = true
        ),
        ::disableMoEngage to booleanPatchOption(
            key = "disableMoEngage",
            default = true,
            values = mapOf(),
            title = "MoEngage",
            description = "",
            required = true
        ),
        ::disableSegment to booleanPatchOption(
            key = "disableSegment",
            default = true,
            values = mapOf(),
            title = "Segment",
            description = "",
            required = true
        ),
    )

    private fun disableStatsig(context: BytecodeContext) {
        StatsigClientFingerprint.resultOrThrow().mutableMethod.addInstructions(0,"return-void")
    }

    private fun disableGoogleAnalytics(context: BytecodeContext) {
        // Empties the "context" argument to force an exception
        AnalyticsInitFingerprint.resultOrThrow().mutableMethod.addInstructions(0,"const/4 p0, 0x0")
    }

    private fun disableAppsFlyer(context: BytecodeContext) {
        AppsFlyerInitFingerprint.resultOrThrow().mutableMethod.addInstructions(
            0,
            """
                    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
                    return-object p0
                    """
        )
    }

    private fun disableComScore(context: BytecodeContext) {
        ComScoreSetupFingerprint.resultOrThrow().mutableMethod.addInstructions(0, "return-void")
    }

    private fun disableCrashlytics(context: BytecodeContext) {
        // Neutralize the two methods responsible for requesting Crashlytics' configuration
        // which effectively disables the SDK

        SettingsSpiCallFingerprint.resultOrThrow().mutableMethod.addInstructions(
            0,
            """
                    const/4 p1, 0x0
                    return-object p1
                    """
        )

        DoConfigFetchFingerprint.resultOrThrow().mutableMethod.addInstructions(
            0,
            """
                    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
                    return-object p1
                    """
        )
    }

    // Prevents the sending of Firebase Logging and Firebase Crashlytics logs to Google's servers.
    private fun disableFirebaseTransport(context: BytecodeContext) {
        // Neutralize the method sending data to the backend
        SendFingerprint.resultOrThrow().mutableMethod.addInstructions(0,"return-void")
    }

    private fun disableMoEngage(context: BytecodeContext) {
        MoEngageInitFingerprint.resultOrThrow().mutableMethod.addInstructions(0, "return-void")
    }

    private fun disableSegment(context: BytecodeContext) {
        // Empties the writeKey parameter to abort initialization
        SegmentBuilderFingerprint.resultOrThrow().mutableMethod.addInstructions(0,"const-string p2, \"\"")
    }


    override fun execute(context: BytecodeContext) {
        subPatchesOptions.forEach {
            if (it.value.value == true){
                try {
                    it.key(context)
                    Logger.getLogger(this::class.java.name).info("Applied privacy patch to disable ${it.value.title}")
                }catch (exception: PatchException){
                    Logger.getLogger(this::class.java.name).info("${it.value.title} not found, skipping...")
                }
            }
        }

    }
}