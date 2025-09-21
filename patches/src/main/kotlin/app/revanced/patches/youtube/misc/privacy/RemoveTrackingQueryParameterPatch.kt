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

@Deprecated("Patch was renamed", ReplaceWith("sanitizeSharingLinksPatch"))
@Suppress("unused")
val removeTrackingQueryParameterPatch = bytecodePatch{
    dependsOn(sanitizeSharingLinksPatch)

//// TODO: Rename this to "Sanitize sharing links" to be consistent with other apps.
//    val removeTrackingQueryParameterPatch = bytecodePatch(
//    name = "Remove tracking query parameter",
//    description = "Adds an option to remove the tracking parameter from links you share.",
//) {
//    dependsOn(
//        sharedExtensionPatch,
//        settingsPatch,
//        addResourcesPatch,
//    )
//
//    compatibleWith(
//        "com.google.android.youtube"(
//            "19.34.42",
//            "19.43.41",
//            "20.07.39",
//            "20.13.41",
//            "20.14.43",
//        )
//    )
//
//    execute {
//        addResources("youtube", "misc.privacy.removeTrackingQueryParameterPatch")
//
//        PreferenceScreen.MISC.addPreferences(
//            SwitchPreference("revanced_remove_tracking_query_parameter"),
//        )
//
//        fun Fingerprint.hookUrlString(matchIndex: Int) {
//            val index = instructionMatches[matchIndex].index
//            val urlRegister = method.getInstruction<OneRegisterInstruction>(index).registerA
//
//            method.addInstructions(
//                index + 1,
//                """
//                    invoke-static { v$urlRegister }, $EXTENSION_CLASS_DESCRIPTOR->sanitize(Ljava/lang/String;)Ljava/lang/String;
//                    move-result-object v$urlRegister
//                """
//            )
//        }
//
//        fun Fingerprint.hookIntentPutExtra(matchIndex: Int) {
//            val index = instructionMatches[matchIndex].index
//            val urlRegister = method.getInstruction<FiveRegisterInstruction>(index).registerE
//
//            method.addInstructionsAtControlFlowLabel(
//                index,
//                """
//                    invoke-static { v$urlRegister }, $EXTENSION_CLASS_DESCRIPTOR->sanitize(Ljava/lang/String;)Ljava/lang/String;
//                    move-result-object v$urlRegister
//                """
//            )
//        }
//
//        // YouTube share sheet copy link.
//        copyTextFingerprint.hookUrlString(0)
//
//        // YouTube share sheet other apps.
//        youtubeShareSheetFingerprint.hookIntentPutExtra(3)
//
//        // Native system share sheet.
//        systemShareSheetFingerprint.hookIntentPutExtra(3)
//    }
//}
