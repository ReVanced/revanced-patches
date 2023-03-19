package app.revanced.patches.youtube.misc.video.quality.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.video.quality.annotations.RememberVideoQualityCompatibility
import app.revanced.patches.youtube.misc.video.quality.fingerprints.SetQualityByIndexMethodClassFieldReferenceFingerprint
import app.revanced.patches.youtube.misc.video.quality.fingerprints.VideoQualityItemOnClickParentFingerprint
import app.revanced.patches.youtube.misc.video.quality.fingerprints.VideoQualitySetterFingerprint
import app.revanced.patches.youtube.misc.video.videoid.patch.VideoIdPatch
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.FieldReference

@Patch
@DependsOn([IntegrationsPatch::class, VideoIdPatch::class, SettingsPatch::class])
@Name("remember-video-quality")
@Description("Adds the ability to remember the video quality you chose in the video quality flyout.")
@RememberVideoQualityCompatibility
@Version("0.0.1")
class RememberVideoQualityPatch : BytecodePatch(
    listOf(
        VideoQualitySetterFingerprint,
        VideoQualityItemOnClickParentFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference(
                "revanced_remember_video_quality_last_selected",
                StringResource(
                    "revanced_remember_video_quality_last_selected_title",
                    "Remember video quality changes"
                ),
                true,
                StringResource(
                    "revanced_remember_video_quality_last_selected_summary_on",
                    "Quality changes apply to all videos"
                ),
                StringResource(
                    "revanced_remember_video_quality_last_selected_summary_off",
                    "Quality changes only apply to the current video"
                )
            )
        )

        /*
         * The following code works by hooking the method which is called when the user selects a video quality
         * to remember the last selected video quality.
         *
         * It also hooks the method which is called when the video quality to set is determined.
         * Conveniently, at this point the video quality is overridden to the remembered playback speed.
         */

        VideoIdPatch.injectCall("$INTEGRATIONS_CLASS_DESCRIPTOR->newVideoStarted(Ljava/lang/String;)V")

        // Inject a call to set the remembered quality once a video loads.
        VideoQualitySetterFingerprint.result?.also {
            if (!SetQualityByIndexMethodClassFieldReferenceFingerprint.resolve(context, it.classDef))
                return PatchResultError("Could not resolve fingerprint to find setQualityByIndex method")
        }?.let {
            // This instruction refers to the field with the type that contains the setQualityByIndex method.
            val instructions = SetQualityByIndexMethodClassFieldReferenceFingerprint.result!!
                .method.implementation!!.instructions

            val getOnItemClickListenerClassReference =
                (instructions.elementAt(0) as ReferenceInstruction).reference
            val getSetQualityByIndexMethodClassFieldReference =
                (instructions.elementAt(1) as ReferenceInstruction).reference

            val setQualityByIndexMethodClassFieldReference =
                getSetQualityByIndexMethodClassFieldReference as FieldReference

            val setQualityByIndexMethodClass = context.classes
                .find { classDef -> classDef.type == setQualityByIndexMethodClassFieldReference.type }!!

            // Get the name of the setQualityByIndex method.
            val setQualityByIndexMethod = setQualityByIndexMethodClass.methods
                .find { method -> method.parameterTypes.first() == "I" }
                ?: return PatchResultError("Could not find setQualityByIndex method")

            it.mutableMethod.addInstructions(
                0,
                """
                    # Get the object instance to invoke the setQualityByIndex method on.
                    iget-object v0, p0, $getOnItemClickListenerClassReference
                    iget-object v0, v0, $getSetQualityByIndexMethodClassFieldReference
                    
                    # Get the method name.
                    const-string v1, "${setQualityByIndexMethod.name}"
                    
                    # Set the quality.
                    # The first parameter is the array list of video qualities.
                    # The second parameter is the index of the selected quality.
                    # The register v0 stores the object instance to invoke the setQualityByIndex method on.
                    # The register v1 stores the name of the setQualityByIndex method.
                    invoke-static {p1, p2, v0, v1}, $INTEGRATIONS_CLASS_DESCRIPTOR->setVideoQuality([Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/String;)I
                    move-result p2
                """,
            )
        } ?: return VideoQualitySetterFingerprint.toErrorResult()

        // Inject a call to remember the selected quality.
        VideoQualityItemOnClickParentFingerprint.result?.let {
            val onItemClickMethod = it.mutableClass.methods.find { method -> method.name == "onItemClick" }

            onItemClickMethod?.apply {
                val listItemIndexParameter = 3

                addInstruction(
                    0,
                    "invoke-static {p$listItemIndexParameter}, $INTEGRATIONS_CLASS_DESCRIPTOR->userChangedQuality(I)V"
                )
            } ?: return PatchResultError("Failed to find onItemClick method")
        } ?: return VideoQualityItemOnClickParentFingerprint.toErrorResult()
        return PatchResultSuccess()
    }

    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/playback/quality/RememberVideoQualityPatch;"
    }
}
