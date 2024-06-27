package app.revanced.patches.soundcloud.ad

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.soundcloud.ad.fingerprints.ConfigureUserPlanFingerprint
import app.revanced.patches.soundcloud.ad.fingerprints.FeaturesFingerprint
import app.revanced.patches.soundcloud.ad.fingerprints.PlanFingerprint
import app.revanced.util.resultOrThrow

@Patch(
    name = "Hide ads",
    compatiblePackages = [CompatiblePackage("com.soundcloud.android")],
)
@Suppress("unused")
object HideAdsPatch : BytecodePatch(
    setOf(FeaturesFingerprint, PlanFingerprint, ConfigureUserPlanFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        // Enables a preset feature to disable audio ads by modifying the JSON server response
        FeaturesFingerprint.resultOrThrow().mutableMethod.apply {
            addInstructionsWithLabels(
                2,
                """
                    const-string v0, "no_audio_ads"
                    invoke-virtual {p1, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                    move-result v0
                    if-eqz v0, :skip
                    const/4 p2, 0x1
                """,
                ExternalLabel("skip", getInstruction(2)),
            )
        }

        // Overwrites the JSON response from the server to a paid plan which hides all ads in the app
        // This does not enable paid features as they are all checked for on the backend
        PlanFingerprint.resultOrThrow().mutableMethod.addInstructions(
            0,
            """
                const-string p1, "high_tier"
                new-instance p4, Ljava/util/ArrayList;
                invoke-direct {p4}, Ljava/util/ArrayList;-><init>()V
                const-string p5, "go-plus"
                const-string p6, "SoundCloud Go+"
            """,
        )

        // Prevents the verification of an HTTP header containing the user's current plan which would contradict the previous patch
        ConfigureUserPlanFingerprint.resultOrThrow().let { result ->
            result.mutableMethod.addInstructions(
                result.scanResult.patternScanResult!!.endIndex,
                """
                       return-object p1
                    """,
            )
        }
    }
}
