package app.revanced.patches.mifitness.misc.locale

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
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

    apply {
        syncBluetoothLanguageMethodMatch.let {
            val resolvePhoneLocaleInstruction = it.indices.first()
            val registerIndexToUpdate = it.method.getInstruction<OneRegisterInstruction>(resolvePhoneLocaleInstruction).registerA

            it.method.replaceInstruction(
                resolvePhoneLocaleInstruction,
                "const-string v$registerIndexToUpdate, \"en_gb\"",
            )
        }
    }
}
