package app.revanced.patches.youtube.misc.quality.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.quality.annotations.DefaultVideoQualityCompatibility
import app.revanced.patches.youtube.misc.quality.fingerprints.VideoQualityReferenceFingerprint
import app.revanced.patches.youtube.misc.quality.fingerprints.VideoQualitySetterFingerprint
import app.revanced.patches.youtube.misc.quality.fingerprints.VideoUserQualityChangeFingerprint
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference
import app.revanced.patches.youtube.misc.videoid.patch.VideoIdPatch
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.FieldReference

@Patch
@DependsOn([IntegrationsPatch::class, VideoIdPatch::class, SettingsPatch::class])
@Name("remember-video-quality")
@Description("Adds the ability to remember the video quality you chose in the video quality flyout.")
@DefaultVideoQualityCompatibility
@Version("0.0.1")
class RememberVideoQualityPatch : BytecodePatch(
    listOf(
        VideoQualitySetterFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference(
                "revanced_remember_video_quality_selection",
                StringResource("revanced_remember_video_quality_selection_title", "Remember current video quality"),
                true,
                StringResource(
                    "revanced_remember_video_quality_selection_summary_on",
                    "The current video quality will not change"
                ),
                StringResource(
                    "revanced_remember_video_quality_selection_summary_off",
                    "Video quality will be remembered until a new quality is chosen"
                )
            )
        )

        val setterMethod = VideoQualitySetterFingerprint.result!!

        VideoUserQualityChangeFingerprint.resolve(context, setterMethod.classDef)
        val userQualityMethod = VideoUserQualityChangeFingerprint.result!!

        VideoQualityReferenceFingerprint.resolve(context, setterMethod.classDef)
        val qualityFieldReference =
            VideoQualityReferenceFingerprint.result!!.method.let { method ->
                (method.implementation!!.instructions.elementAt(0) as ReferenceInstruction).reference as FieldReference
            }

        VideoIdPatch.injectCall("Lapp/revanced/integrations/patches/VideoQualityPatch;->newVideoStarted(Ljava/lang/String;)V")

        val qIndexMethodName =
            context.classes.single { it.type == qualityFieldReference.type }.methods.single { it.parameterTypes.first() == "I" }.name

        setterMethod.mutableMethod.addInstructions(
            0,
            """
                iget-object v0, p0, ${setterMethod.classDef.type}->${qualityFieldReference.name}:${qualityFieldReference.type}
                const-string v1, "$qIndexMethodName"
		        invoke-static {p1, p2, v0, v1}, Lapp/revanced/integrations/patches/VideoQualityPatch;->setVideoQuality([Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/String;)I
   		        move-result p2
            """,
        )

        userQualityMethod.mutableMethod.addInstruction(
            0,
            "invoke-static {p3}, Lapp/revanced/integrations/patches/VideoQualityPatch;->userChangedQuality(I)V"
        )

        return PatchResultSuccess()
    }
}
