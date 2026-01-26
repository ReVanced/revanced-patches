package app.revanced.patches.tiktok.interaction.speed

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tiktok.misc.extension.sharedExtensionPatch
import app.revanced.patches.tiktok.shared.getEnterFromFingerprint
import app.revanced.patches.tiktok.shared.onRenderFirstFrameFingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction11x
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Suppress("unused")
val playbackSpeedPatch = bytecodePatch(
    name = "Playback speed",
    description = "Enables the playback speed option for all videos and " +
        "retains the speed configurations in between videos.",
) {
    dependsOn(sharedExtensionPatch)

    compatibleWith(
        "com.ss.android.ugc.trill"("43.6.2"),
        "com.zhiliaoapp.musically"("43.6.2"),
    )

    execute {
        getSpeedFingerprint.method.apply {
            val injectIndex =
                indexOfFirstInstructionOrThrow { getReference<MethodReference>()?.returnType == "F" } + 2
            val register = getInstruction<Instruction11x>(injectIndex - 1).registerA

            addInstruction(
                injectIndex,
                "invoke-static { v$register }," +
                    " Lapp/revanced/extension/tiktok/speed/PlaybackSpeedPatch;->rememberPlaybackSpeed(F)V",
            )
        }

        // By default, the playback speed will reset to 1.0 at the start of each video.
        // Instead, override it with the desired playback speed.
        onRenderFirstFrameFingerprint.method.addInstructions(
            0,
            """
                # Video playback location (e.g. home page, following page or search result page) retrieved using getEnterFrom method.
                const/4 v0, 0x1
                invoke-virtual { p0, v0 }, ${getEnterFromFingerprint.originalMethod}
                move-result-object v0

                # Model of current video retrieved using getCurrentAweme method.
                invoke-virtual { p0 }, Lcom/ss/android/ugc/aweme/feed/panel/BaseListFragmentPanel;->getCurrentAweme()Lcom/ss/android/ugc/aweme/feed/model/Aweme;
                move-result-object v1
                if-eqz v1, :revanced_skip_set_speed

                # Desired playback speed retrieved using getPlaybackSpeed method.
                invoke-static {}, Lapp/revanced/extension/tiktok/speed/PlaybackSpeedPatch;->getPlaybackSpeed()F
                move-result v2

                # Apply desired playback speed.
                const/4 v3, 0x0
                invoke-static { v0, v1, v2, v3 }, LX/0MbX;->LJ(Ljava/lang/String;Lcom/ss/android/ugc/aweme/feed/model/Aweme;FLjava/lang/String;)V

                :revanced_skip_set_speed
                nop
            """,
        )

        // Force enable the playback speed option for all videos.
        speedOptionEnabledFingerprint.method.addInstructions(
            0,
            """
                if-eqz p0, :revanced_return_false
                const/4 v0, 0x1
                return v0

                :revanced_return_false
                const/4 v0, 0x0
                return v0
            """,
        )
    }
}
