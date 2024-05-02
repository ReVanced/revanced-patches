package app.revanced.patches.tiktok.interaction.speed

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tiktok.interaction.speed.fingerprints.getSpeedFingerprint
import app.revanced.patches.tiktok.interaction.speed.fingerprints.onRenderFirstFrameFingerprint
import app.revanced.patches.tiktok.interaction.speed.fingerprints.setSpeedFingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction11x
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Suppress("unused")
val playbackSpeedPatch = bytecodePatch(
    name = "Playback speed",
    description = "Enables the playback speed option for all videos and " +
            "retains the speed configurations in between videos.",
) {
    compatibleWith("com.ss.android.ugc.trill"("32.5.3"), "com.zhiliaoapp.musically"("32.5.3"))

    val getSpeedResult by getSpeedFingerprint
    val onRenderFirstFrameResult by onRenderFirstFrameFingerprint
    val setSpeedResult by setSpeedFingerprint

    execute {
        setSpeedResult.let { onVideoSwiped ->
            getSpeedResult.mutableMethod.apply {
                val injectIndex = indexOfFirstInstruction { getReference<MethodReference>()?.returnType == "F" } + 2
                val register = getInstruction<Instruction11x>(injectIndex - 1).registerA

                addInstruction(
                    injectIndex,
                    "invoke-static { v$register }," +
                            " Lapp/revanced/integrations/tiktok/speed/PlaybackSpeedPatch;->rememberPlaybackSpeed(F)V",
                )
            }

            // By default, the playback speed will reset to 1.0 at the start of each video.
            // Instead, override it with the desired playback speed.
            onRenderFirstFrameResult.mutableMethod.addInstructions(
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
            """,
            )

            // Force enable the playback speed option for all videos.
            onVideoSwiped.mutableClass.methods.find { method -> method.returnType == "Z" }?.addInstructions(
                0,
                """
                const/4 v0, 0x1
                return v0
            """,
            )
        }
    }
}
