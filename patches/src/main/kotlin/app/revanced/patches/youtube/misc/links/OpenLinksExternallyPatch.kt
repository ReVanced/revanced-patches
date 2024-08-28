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

@Suppress("unused")
val openLinksExternallyPatch = bytecodePatch(
    name = "Open links externally",
    description = "Adds an option to always open links in your browser instead of in the in-app-browser.",
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
        ),
    )

    execute {
        addResources("youtube", "misc.links.openLinksExternallyPatch")

        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_external_browser"),
        )
    }
}
