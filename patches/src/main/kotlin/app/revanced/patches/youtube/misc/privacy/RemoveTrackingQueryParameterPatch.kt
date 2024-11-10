package app.revanced.patches.youtube.misc.privacy

import app.revanced.patcher.Fingerprint
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
            "18.38.44",
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
        ),
    )

    execute {
        addResources("youtube", "misc.privacy.removeTrackingQueryParameterPatch")

        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_remove_tracking_query_parameter"),
        )

        suspend fun Fingerprint.hook(
            getInsertIndex: Match.PatternMatch.() -> Int,
            getUrlRegister: MutableMethod.(insertIndex: Int) -> Int,
        ) {
            val insertIndex = patternMatch()!!.getInsertIndex()
            val urlRegister = method().getUrlRegister(insertIndex)

            method().addInstructions(
                insertIndex,
                """
                    invoke-static {v$urlRegister}, $EXTENSION_CLASS_DESCRIPTOR->sanitize(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$urlRegister
                """,
            )
        }

        // YouTube share sheet.\
        youtubeShareSheetFingerprint.hook(getInsertIndex = { startIndex + 1 }) { insertIndex ->
            getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA
        }

        // Native system share sheet.
        systemShareSheetFingerprint.hook(getInsertIndex = { endIndex }) { insertIndex ->
            getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA
        }

        copyTextFingerprint.hook(getInsertIndex = { startIndex + 2 }) { insertIndex ->
            getInstruction<TwoRegisterInstruction>(insertIndex - 2).registerA
        }
    }
}
