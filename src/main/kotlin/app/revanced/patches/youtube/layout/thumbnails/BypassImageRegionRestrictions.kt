package app.revanced.patches.youtube.layout.thumbnails

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.imageurlhook.CronetImageUrlHook
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    name = "Bypass image region restrictions",
    description = "Adds an option to use a different host for user avatar and channel images " +
            "and can fix missing images that are blocked in some countries.",
    dependencies = [
        IntegrationsPatch::class,
        SettingsPatch::class,
        AddResourcesPatch::class,
        CronetImageUrlHook::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.38.44",
                "18.49.37",
                "19.16.39",
                "19.25.37",
                "19.34.42",
            ]
        )
    ]
)
@Suppress("unused")
object BypassImageRegionRestrictions : BytecodePatch(emptySet()) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/BypassImageRegionRestrictionsPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_bypass_image_region_restrictions")
        )

        // A priority hook is not needed, as the image urls of interest are not modified
        // by AlternativeThumbnails or any other patch in this repo.
        CronetImageUrlHook.addImageUrlHook(INTEGRATIONS_CLASS_DESCRIPTOR)
    }
}
