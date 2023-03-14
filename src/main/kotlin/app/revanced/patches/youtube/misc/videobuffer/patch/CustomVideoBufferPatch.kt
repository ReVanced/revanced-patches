package app.revanced.patches.youtube.misc.videobuffer.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.settings.preference.impl.*
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.videobuffer.annotations.CustomVideoBufferCompatibility

// TODO: delete this patch
@Patch(include = false)
@Name("custom-video-buffer")
@Description("Lets you change the buffers of videos.")
@DependsOn([SettingsPatch::class])
@CustomVideoBufferCompatibility
@Version("0.0.1")
class CustomVideoBufferPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            NonInteractivePreference(
                StringResource("revanced_custom_video_buffer_disclaimer_title", "Custom video buffer"),
                StringResource("revanced_custom_video_buffer_disclaimer_title_summary",
                    "These settings have been removed, because they were not functional for the duration of their existence"),
            )
        )

        return PatchResultSuccess()
    }
}

