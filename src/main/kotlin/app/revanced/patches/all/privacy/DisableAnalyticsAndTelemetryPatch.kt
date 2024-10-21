package app.revanced.patches.all.privacy

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.privacy.fingerprints.*
import app.revanced.util.resultOrThrow
import java.util.logging.Logger

@Patch(
    name = "Disable analytics and telemetry",
    description = "Patches the app to disable analytics and telemetry services.",
    use = false,
)
@Suppress("unused")
object DisableAnalyticsAndTelemetryPatch : BytecodePatch(
    setOf(
        InitializeAsyncStatsigClientFingerprint,
        InitAnalyticsFingerprint,
        InitAppsFlyerSDKFingerprint,
        SetupComsCoreFingerprint,
        CallSettingsSpiFingerprint,
        DoConfigFetchFingerprint,
        SendFingerprint,
        InitializeSdkFingerprint,
        BuildSegmentFingerprint,
        InitSDKFingerprint,
        InitAmplitudeFingerprint,
    ),
) {
    private val logger = Logger.getLogger(this::class.java.name)

    private val disableGoogleAnalytics by option("Google Analytics")
    private val disableAmplitude by option("Amplitude")
    private val disableStatsig by option("Statsig")
    private val disableAppsFlyerSDK by option("Apps Flyer SDK")
    private val disableAppsFlyerPlugin by option("Apps Flyer plugin")
    private val disableComsCore by option("ComsCore")
    private val disableCrashlytics by option("Crashlytics")
    private val disableFirebaseTransport by option("Firebase Transport")
    private val disableMoEngage by option("MoEngage")
    private val disableSegment by option("Segment")

    override fun execute(context: BytecodeContext) = mapOf(
        disableAmplitude to {
            InitAmplitudeFingerprint.resultOrThrow().mutableMethod.addInstructions(
                0,
                "return-object p0",
            )
        },
        disableStatsig to {
            InitializeAsyncStatsigClientFingerprint.resultOrThrow().mutableMethod.addInstructions(
                0,
                "return-void",
            )
        },
        disableAppsFlyerSDK to {
            InitAppsFlyerSDKFingerprint.resultOrThrow().mutableMethod.addInstructions(
                0,
                "return-object p0",
            )
        },
        disableAppsFlyerPlugin to {
            InitSDKFingerprint.resultOrThrow().mutableMethod.addInstructions(
                0,
                "return-void",
            )
        },
        disableComsCore to {
            SetupComsCoreFingerprint.resultOrThrow().mutableMethod.addInstructions(
                0,
                "return-void",
            )
        },
        // Neutralize the method sending data to the backend.
        disableFirebaseTransport to {
            SendFingerprint.resultOrThrow().mutableMethod.addInstructions(
                0,
                "return-void",
            )
        },
        // Empties the "context" argument to force an exception.
        disableGoogleAnalytics to {
            InitAnalyticsFingerprint.resultOrThrow().mutableMethod.addInstructions(
                0,
                "const/4 p0, 0x0",
            )
        },
        // Empties the writeKey parameter to abort initialization
        disableSegment to {
            BuildSegmentFingerprint.resultOrThrow().mutableMethod.addInstructions(
                0,
                "const-string p2, \"\"",
            )
        },
        // Neutralize the two methods responsible for requesting Crashlytics' configuration.
        // which effectively disables the SDK.
        disableCrashlytics to {
            CallSettingsSpiFingerprint.resultOrThrow().mutableMethod.addInstructions(
                0,
                """
                    const/4 p1, 0x0
                    return-object p1
                """,
            )

            DoConfigFetchFingerprint.resultOrThrow().mutableMethod.apply {
                // Jumps to the end of the method to directly return an empty object.
                addInstructionsWithLabels(
                    0,
                    """
                        goto :return_unit
                    """,
                    ExternalLabel("return_unit", getInstruction(getInstructions().lastIndex - 2)),
                )
            }
        },
        disableMoEngage to {
            InitializeSdkFingerprint.resultOrThrow().mutableMethod.addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return-object v0
                """,
            )
        },
    ).forEach { (option, patch) ->
        val isEnabled by option
        if (!isEnabled!!) return@forEach

        val message = try {
            patch()

            "Disabled ${option.title}"
        } catch (exception: PatchException) {
            "${option.title} was not found. Skipping."
        }

        logger.info(message)
    }
}
