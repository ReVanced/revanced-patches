package app.revanced.patches.youtube.misc.links

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.transformation.transformInstructionsPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

val openLinksExternallyPatch = bytecodePatch(
    name = "Open links externally",
    description = "Adds an option to always open links in your browser instead of the in-app browser.",
) {
    dependsOn(
        transformInstructionsPatch(
            filterMap = filterMap@{ _, _, instruction, instructionIndex ->
                if (instruction !is ReferenceInstruction) return@filterMap null
                val reference = instruction.reference as? StringReference ?: return@filterMap null

                if (reference.string != "android.support.customtabs.action.CustomTabsService") return@filterMap null

                return@filterMap instructionIndex to (instruction as OneRegisterInstruction).registerA
            },
            transform = { mutableMethod, entry ->
                val (intentStringIndex, register) = entry

                // Hook the intent string.
                mutableMethod.addInstructions(
                    intentStringIndex + 1,
                    """
                        invoke-static {v$register}, Lapp/revanced/extension/youtube/patches/OpenLinksExternallyPatch;->getIntent(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$register
                    """,
                )
            },
        ),
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.34.42",
            "20.07.39",
            "20.13.41",
            "20.14.43",
        )
    )

    execute {
        addResources("youtube", "misc.links.openLinksExternallyPatch")

        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_external_browser"),
        )
    }
}
