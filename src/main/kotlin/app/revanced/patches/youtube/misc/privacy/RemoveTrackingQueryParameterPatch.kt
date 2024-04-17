package app.revanced.patches.youtube.misc.privacy

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.fingerprint.MethodFingerprintResult.MethodFingerprintScanResult.PatternScanResult
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.privacy.fingerprints.CopyTextFingerprint
import app.revanced.patches.youtube.misc.privacy.fingerprints.SystemShareSheetFingerprint
import app.revanced.patches.youtube.misc.privacy.fingerprints.YouTubeShareSheetFingerprint
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(
    name = "Remove tracking query parameter",
    description = "Adds an option to remove the tracking info from links you share.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class, AddResourcesPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
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
                "19.11.43"
            ]
        )
    ]
)
@Suppress("unused")
object RemoveTrackingQueryParameterPatch : BytecodePatch(
    setOf(CopyTextFingerprint, SystemShareSheetFingerprint, YouTubeShareSheetFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/RemoveTrackingQueryParameterPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_remove_tracking_query_parameter")
        )

        fun MethodFingerprint.hook(
            getInsertIndex: PatternScanResult.() -> Int,
            getUrlRegister: MutableMethod.(insertIndex: Int) -> Int
        ) = result?.let {
            val insertIndex = it.scanResult.patternScanResult!!.getInsertIndex()
            val urlRegister = it.mutableMethod.getUrlRegister(insertIndex)

            it.mutableMethod.addInstructions(
                insertIndex,
                """
                    invoke-static {v$urlRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->sanitize(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$urlRegister
                """
            )
        } ?: throw exception

        // Native YouTube share sheet.
        YouTubeShareSheetFingerprint.hook(getInsertIndex = { startIndex + 1 })
        { insertIndex -> getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA }

        // Native system share sheet.
        SystemShareSheetFingerprint.hook(getInsertIndex = { endIndex })
        { insertIndex -> getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA }

        CopyTextFingerprint.hook(getInsertIndex = { startIndex + 2 })
        { insertIndex -> getInstruction<TwoRegisterInstruction>(insertIndex - 2).registerA }
    }
}