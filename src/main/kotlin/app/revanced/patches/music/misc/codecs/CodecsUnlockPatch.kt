package app.revanced.patches.music.misc.codecs

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.utils.integrations.Constants.MISC_PATH
import app.revanced.patches.music.utils.settings.CategoryType
import app.revanced.patches.music.utils.settings.SettingsPatch
import app.revanced.patches.shared.patch.opus.AbstractOpusCodecsPatch

@Patch(
    name = "Enable opus codec",
    description = "Adds an option use the opus audio codec instead of the mp4a audio codec.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object CodecsUnlockPatch : AbstractOpusCodecsPatch(
    "$MISC_PATH/OpusCodecPatch;->enableOpusCodec()Z"
) {
    override fun execute(context: BytecodeContext) {
        super.execute(context)

        SettingsPatch.addMusicPreference(
            CategoryType.MISC,
            "revanced_enable_opus_codec",
            "true"
        )

    }
}
