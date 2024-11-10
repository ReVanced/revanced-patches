package app.revanced.patches.youtube.misc.autorepeat

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.shared.autoRepeatFingerprint
import app.revanced.patches.youtube.shared.autoRepeatParentFingerprint
import org.stringtemplate.v4.compiler.Bytecode.instructions

// TODO: Rename this patch to AlwaysRepeatPatch (as well as strings and references in the extension).
@Suppress("unused")
val autoRepeatPatch = bytecodePatch(
    name = "Always repeat",
    description = "Adds an option to always repeat videos when they end.",
) {
    dependsOn(
        sharedExtensionPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.38.44",
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
        ),
    )

    execute {
        addResources("youtube", "misc.autorepeat.autoRepeatPatch")

        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_auto_repeat"),
        )

        autoRepeatFingerprint.match(autoRepeatParentFingerprint.originalClassDef()).method.apply {
            val playMethod = autoRepeatParentFingerprint.method()
            val index = instructions.lastIndex

            // Remove return-void.
            removeInstruction(index)
            // Add own instructions there.
            addInstructionsWithLabels(
                index,
                """
                    invoke-static {}, Lapp/revanced/extension/youtube/patches/AutoRepeatPatch;->shouldAutoRepeat()Z
                    move-result v0
                    if-eqz v0, :noautorepeat
                    invoke-virtual { p0 }, $playMethod
                    :noautorepeat
                    return-void
                """,
            )
        }
    }
}
