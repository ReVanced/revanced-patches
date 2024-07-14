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
    description = "Adds an option to use a different host for user avatar and channel images," +
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
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39",
                "19.03.36",
                "19.04.38",
                "19.05.36",
                "19.06.39",
                "19.07.40",
                "19.08.36",
                "19.09.38",
                "19.10.39",
                "19.11.43",
                "19.12.41",
                "19.13.37",
                "19.14.43",
                "19.15.36",
                "19.16.39",
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

        SettingsPatch.PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_bypass_image_region_restrictions")
        )

        // A priority hook is not needed, as the image urls of interest are not modified
        // by AlternativeThumbnails or any other patch in this repo.
        CronetImageUrlHook.addImageUrlHook(INTEGRATIONS_CLASS_DESCRIPTOR)
    }
}
