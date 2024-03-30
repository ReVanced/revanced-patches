package app.revanced.patches.youtube.misc.announcements

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.shared.fingerprints.MainActivityOnCreateFingerprint
import app.revanced.util.resultOrThrow

@Patch(
    name = "Announcements",
    description = "Adds an option to show announcements from ReVanced on app startup.",
    compatiblePackages = [CompatiblePackage("com.google.android.youtube")],
    dependencies = [SettingsPatch::class,AddResourcesPatch::class]
)
@Suppress("unused")
object AnnouncementsPatch : BytecodePatch(
    setOf(MainActivityOnCreateFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/announcements/AnnouncementsPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_announcements")
        )

        MainActivityOnCreateFingerprint.resultOrThrow().mutableMethod.addInstructions(
            // Insert index must be great than the insert index used by GmsCoreSupport,
            // as both patch the same method and GmsCore check should be first.
            1,
            "invoke-static/range { p0 .. p0 }, $INTEGRATIONS_CLASS_DESCRIPTOR->showAnnouncement(Landroid/app/Activity;)V"
        )
    }
}
