package app.revanced.patches.mifitness.misc.locale

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.mifitness.misc.login.`Fix login`
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused", "ObjectPropertyName")
val `Force English locale` by creatingBytecodePatch(
    description = "Forces wearable devices to use the English locale.",
) {
    compatibleWith("com.xiaomi.wearable")

    dependsOn(`Fix login`)

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
