package app.revanced.patches.youtube.misc.quality.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.quality.annotations.DefaultVideoQualityCompatibility
import app.revanced.patches.youtube.misc.quality.fingerprints.VideoQualityReferenceFingerprint
import app.revanced.patches.youtube.misc.quality.fingerprints.VideoQualitySetterFingerprint
import app.revanced.patches.youtube.misc.quality.fingerprints.VideoUserQualityChangeFingerprint
import app.revanced.patches.youtube.misc.videoid.patch.VideoIdPatch
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.FieldReference

@Patch
@DependsOn([IntegrationsPatch::class, VideoIdPatch::class])
@Name("remember-video-quality")
@Description("Adds the ability to remember the video quality you chose in the video quality flyout.")
@DefaultVideoQualityCompatibility
@Version("0.0.1")
class RememberVideoQualityPatch : BytecodePatch(
    listOf(
        VideoQualitySetterFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        //TODO: include setting to skip remembering the new quality

        val setterMethod = VideoQualitySetterFingerprint.result!!

        VideoUserQualityChangeFingerprint.resolve(data, setterMethod.classDef)
        val userQualityMethod = VideoUserQualityChangeFingerprint.result!!

        VideoQualityReferenceFingerprint.resolve(data, setterMethod.classDef)
        val qualityFieldReference =
            VideoQualityReferenceFingerprint.result!!.method.let { method ->
                (method.implementation!!.instructions.elementAt(0) as ReferenceInstruction).reference as FieldReference
            }

        VideoIdPatch.injectCall("Lapp/revanced/integrations/patches/VideoQualityPatch;->newVideoStarted(Ljava/lang/String;)V")

        val qIndexMethodName =
            data.classes.single { it.type == qualityFieldReference.type }.methods.single { it.parameterTypes.first() == "I" }.name

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
