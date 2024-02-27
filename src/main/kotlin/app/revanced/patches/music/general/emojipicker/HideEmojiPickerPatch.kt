package app.revanced.patches.music.general.emojipicker

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.utils.integrations.Constants.COMPONENTS_PATH
import app.revanced.patches.music.utils.litho.LithoFilterPatch
import app.revanced.patches.music.utils.settings.CategoryType
import app.revanced.patches.music.utils.settings.SettingsPatch

@Patch(
    name = "Hide emoji picker and time stamp",
    description = "Adds an option to hide the emoji picker and time stamp when typing comments.",
    dependencies = [
        LithoFilterPatch::class,
        SettingsPatch::class
    ],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object HideEmojiPickerPatch : BytecodePatch(emptySet()) {
    override fun execute(context: BytecodeContext) {

        SettingsPatch.addMusicPreference(
            CategoryType.GENERAL,
            "revanced_hide_emoji_picker",
            "false"
        )

        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)

    }

    private const val FILTER_CLASS_DESCRIPTOR =
        "$COMPONENTS_PATH/EmojiPickerFilter;"
}
