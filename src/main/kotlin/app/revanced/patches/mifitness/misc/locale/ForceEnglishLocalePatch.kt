package app.revanced.patches.mifitness.misc.locale

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.mifitness.misc.login.fixLoginPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val forceEnglishLocalePatch = bytecodePatch(
    name = "Force English locale",
    description = "Forces wearable devices to use the English locale.",
) {
    compatibleWith("com.xiaomi.wearable")

    dependsOn(fixLoginPatch)

    val syncBluetoothLanguageFingerprintResult by syncBluetoothLanguageFingerprint()

    execute {
        val resolvePhoneLocaleInstruction = syncBluetoothLanguageFingerprintResult.scanResult.patternScanResult!!.startIndex

        syncBluetoothLanguageFingerprintResult.mutableMethod.apply {
            val registerIndexToUpdate =
                getInstruction<OneRegisterInstruction>(resolvePhoneLocaleInstruction).registerA

            replaceInstruction(
                resolvePhoneLocaleInstruction,
                "const-string v$registerIndexToUpdate, \"en_gb\"",
            )
        }
    }
}
