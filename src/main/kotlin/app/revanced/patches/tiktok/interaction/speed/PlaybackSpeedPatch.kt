package app.revanced.patches.tiktok.interaction.speed

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.tiktok.interaction.speed.fingerprints.GetSpeedFingerprint
import app.revanced.patches.tiktok.interaction.speed.fingerprints.OnRenderFirstFrameFingerprint
import app.revanced.patches.tiktok.interaction.speed.fingerprints.SetSpeedFingerprint
import app.revanced.util.exception
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction11x
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Playback speed",
    description = "Enables the playback speed option for all videos and " +
            "retains the speed configurations in between videos.",
    compatiblePackages = [
        CompatiblePackage("com.ss.android.ugc.trill", ["32.5.3"]),
        CompatiblePackage("com.zhiliaoapp.musically", ["32.5.3"])
    ]
)
@Suppress("unused")
object PlaybackSpeedPatch : BytecodePatch(
    setOf(
        GetSpeedFingerprint,
        OnRenderFirstFrameFingerprint,
        SetSpeedFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        SetSpeedFingerprint.result?.let { onVideoSwiped ->
            // Remember the playback speed of the current video.
            GetSpeedFingerprint.result?.mutableMethod?.apply {
                val injectIndex = indexOfFirstInstruction { getReference<MethodReference>()?.returnType == "F" } + 2
                val register = getInstruction<Instruction11x>(injectIndex - 1).registerA

                addInstruction(
                    injectIndex,
                    "invoke-static { v$register }," +
                            " Lapp/revanced/integrations/tiktok/speed/PlaybackSpeedPatch;->rememberPlaybackSpeed(F)V"
                )
            } ?: throw GetSpeedFingerprint.exception

            // By default, the playback speed will reset to 1.0 at the start of each video.
            // Instead, override it with the desired playback speed.
            OnRenderFirstFrameFingerprint.result?.mutableMethod?.addInstructions(
                0,
                """
                # Video playback location (e.g. home page, following page or search result page) retrieved using getEnterFrom method.
                const/4 v0, 0x1
                invoke-virtual {p0, v0}, Lcom/ss/android/ugc/aweme/feed/panel/BaseListFragmentPanel;->getEnterFrom(Z)Ljava/lang/String;
                move-result-object v0

                # Model of current video retrieved using getCurrentAweme method.
                invoke-virtual {p0}, Lcom/ss/android/ugc/aweme/feed/panel/BaseListFragmentPanel;->getCurrentAweme()Lcom/ss/android/ugc/aweme/feed/model/Aweme;
                move-result-object v1

                # Desired playback speed retrieved using getPlaybackSpeed method.
                invoke-static {}, Lapp/revanced/integrations/tiktok/speed/PlaybackSpeedPatch;->getPlaybackSpeed()F
                move-result-object v2
                invoke-static { v0, v1, v2 }, ${onVideoSwiped.method}
            """
            ) ?: throw OnRenderFirstFrameFingerprint.exception

            // Force enable the playback speed option for all videos.
            onVideoSwiped.mutableClass.methods.find { method -> method.returnType == "Z" }?.addInstructions(
                0,
                """
                const/4 v0, 0x1
                return v0
            """
            ) ?: throw PatchException("Failed to force enable the playback speed option.")
        } ?: throw SetSpeedFingerprint.exception
    }
}
