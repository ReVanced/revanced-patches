package app.revanced.patches.youtube.misc.announcements

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.shared.fingerprints.MainActivityFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode

@Patch(
    name = "Announcements",
    description = "Adds an option to show announcements from ReVanced on app startup.",
    compatiblePackages = [CompatiblePackage("com.google.android.youtube")],
    dependencies = [SettingsPatch::class,AddResourcesPatch::class]
)
@Suppress("unused")
object AnnouncementsPatch : BytecodePatch(
    setOf(MainActivityFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/announcements/AnnouncementsPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.MISC.addPreferences(SwitchPreference("revanced_announcements"))

        val onCreateMethod = MainActivityFingerprint.result?.let {
            it.mutableClass.methods.find { method -> method.name == "onCreate" }
        } ?: throw MainActivityFingerprint.exception

        val superCallIndex = onCreateMethod.getInstructions().indexOfFirst { it.opcode == Opcode.INVOKE_SUPER_RANGE }

        onCreateMethod.addInstructions(
            superCallIndex + 1,
            "invoke-static { v1 }, $INTEGRATIONS_CLASS_DESCRIPTOR->showAnnouncement(Landroid/app/Activity;)V"
        )

    }
}
