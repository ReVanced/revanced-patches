package app.revanced.patches.tiktok.interaction.speed

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.tiktok.interaction.speed.fingerprints.ChangeSpeedFingerprint
import app.revanced.patches.tiktok.interaction.speed.fingerprints.GetSpeedFingerprint
import app.revanced.patches.tiktok.interaction.speed.fingerprints.OnRenderFirstFrameFingerprint
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
        ChangeSpeedFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        val changeSpeedMethod = ChangeSpeedFingerprint.result?.mutableMethod
            ?: throw ChangeSpeedFingerprint.exception

        // Enables playback speed option for all videos.
        val enableSpeedControlMethod =
            context.findClass(changeSpeedMethod.definingClass)?.mutableClass?.methods?.first {
                it.returnType == "Z"
            }
        enableSpeedControlMethod?.addInstructions(
            0,
            """
                    const/4 v0, 0x1
                    return v0
                """
        )

        // Catches the playback speed changed event at current video and saves it to apply to other videos.
        GetSpeedFingerprint.result?.mutableMethod?.apply {
            val injectIndex = indexOfFirstInstruction { getReference<MethodReference>()?.returnType == "F" } + 2
            val register = getInstruction<Instruction11x>(injectIndex - 1).registerA

            addInstruction(
                injectIndex,
                "invoke-static { v$register }," +
                        " Lapp/revanced/tiktok/speed/SpeedPatch;->rememberPlaybackSpeed(F)V"
            )
        } ?: throw GetSpeedFingerprint.exception

        // Changes current video playback speed at the first frame of video using the saved playback speed.
        // Because the default behavior of the TikTok app is that playback speed will reset to 1.0
        // when swiping to the next video.
        OnRenderFirstFrameFingerprint.result?.mutableMethod?.addInstructions(
            0,
            """
                # The changeSpeedMethod have 3 arguments.
                # First argument is a String. It changed depend on where video playing such as home page, following page, search result page, ...
                # We are able to get it use getEnterFrom method.
                const/4 v0, 0x1
                invoke-virtual {p0, v0}, Lcom/ss/android/ugc/aweme/feed/panel/BaseListFragmentPanel;->getEnterFrom(Z)Ljava/lang/String;
                move-result-object v0
                # Second argument is a Aweme. It is some kind of data for a TikTok video.
                # We can get it use getCurrentAweme method.
                invoke-virtual {p0}, Lcom/ss/android/ugc/aweme/feed/panel/BaseListFragmentPanel;->getCurrentAweme()Lcom/ss/android/ugc/aweme/feed/model/Aweme;
                move-result-object v1
                # Third argument is a float. It is the playback speed value to apply to the TikTok video.
                invoke-static {}, Lapp/revanced/tiktok/speed/SpeedPatch;->getPlaybackSpeed()F
                move-result-object v2
                invoke-static { v0, v1, v2 }, $changeSpeedMethod
            """
        ) ?: throw OnRenderFirstFrameFingerprint.exception
    }
}