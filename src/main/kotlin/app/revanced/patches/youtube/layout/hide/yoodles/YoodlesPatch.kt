package app.revanced.patches.youtube.layout.hide.yoodles

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.hide.yoodles.fingerprints.YoodlesImageViewFingerprint
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.findOpcodeIndicesReversed
import app.revanced.util.getReference
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

// Yes, YouTube gave this feature the goofy name of "Yoodles".
// https://logos.fandom.com/wiki/YouTube/Yoodles
@Patch(
    name = "Hide Yoodles",
    description = "Adds options to hide the YouTube doodle that sometimes replaces the YouTube logo beside the search bar.",
    dependencies = [
        YoodlesResourcePatch::class,
        SettingsPatch::class,
        AddResourcesPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
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
            ],
        ),
    ],
)
@Suppress("unused")
object YoodlesPatch : BytecodePatch(setOf(YoodlesImageViewFingerprint)) {

    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/yoodles/YoodlesPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.FEED.addPreferences(
            SwitchPreference("revanced_hide_yoodles"),
        )

        YoodlesImageViewFingerprint.resultOrThrow().mutableMethod.apply {
            findOpcodeIndicesReversed{
                opcode == Opcode.INVOKE_VIRTUAL
                        && getReference<MethodReference>()?.name == "setImageDrawable"
            }.forEach {
                removeInstruction(it)
            }
        }
    }
}