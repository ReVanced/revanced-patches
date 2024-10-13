package app.revanced.patches.youtube.misc.privacy

import app.revanced.patcher.Match
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/RemoveTrackingQueryParameterPatch;"

@Suppress("unused")
val removeTrackingQueryParameterPatch = bytecodePatch(
    name = "Remove tracking query parameter",
    description = "Adds an option to remove the tracking info from links you share.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
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

    val youTubeShareSheetMatch by youtubeShareSheetFingerprint()
    val systemShareSheetMatch by systemShareSheetFingerprint()
    val copyTextMatch by copyTextFingerprint()

    execute {
        addResources("youtube", "misc.privacy.removeTrackingQueryParameterPatch")

        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_remove_tracking_query_parameter"),
        )

        fun Match.hook(
            getInsertIndex: Match.PatternMatch.() -> Int,
            getUrlRegister: MutableMethod.(insertIndex: Int) -> Int,
        ) {
            val insertIndex = patternMatch!!.getInsertIndex()
            val urlRegister = mutableMethod.getUrlRegister(insertIndex)

            mutableMethod.addInstructions(
                insertIndex,
                """
                    invoke-static {v$urlRegister}, $EXTENSION_CLASS_DESCRIPTOR->sanitize(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$urlRegister
                """,
            )
        }

        // YouTube share sheet.
        youTubeShareSheetMatch.hook(getInsertIndex = { startIndex + 1 }) { insertIndex ->
            getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA
        }

        // Native system share sheet.
        systemShareSheetMatch.hook(getInsertIndex = { endIndex }) { insertIndex ->
            getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA
        }

        copyTextMatch.hook(getInsertIndex = { startIndex + 2 }) { insertIndex ->
            getInstruction<TwoRegisterInstruction>(insertIndex - 2).registerA
        }
    }
}
