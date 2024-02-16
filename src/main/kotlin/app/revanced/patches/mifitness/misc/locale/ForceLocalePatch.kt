package app.revanced.patches.mifitness.misc.locale

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.patches.mifitness.misc.locale.fingerprints.SyncBluetoothLanguageFingerprint
import app.revanced.patches.mifitness.misc.login.FixLoginPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Force locale",
    description = "Forces the locale to a specific language",
    compatiblePackages = [CompatiblePackage("com.xiaomi.wearable")],
    dependencies = [FixLoginPatch::class],
)
@Suppress("unused")
object ForceLocalePatch : BytecodePatch(
    setOf(SyncBluetoothLanguageFingerprint),
) {
    private val locale by stringPatchOption(
        "locale",
        "en_gb",
        null,
        "Locale",
        "The locale to force the app to use.",
        true,
    )

    override fun execute(context: BytecodeContext) {
        SyncBluetoothLanguageFingerprint.result?.apply {
            val instructionIndex = scanResult.patternScanResult!!.startIndex

            mutableMethod.apply {
                val registerIndexToUpdate = getInstruction<OneRegisterInstruction>(instructionIndex).registerA

                replaceInstruction(
                    instructionIndex,
                    "const-string v$registerIndexToUpdate, \"$locale\"",
                )
            }
        } ?: throw SyncBluetoothLanguageFingerprint.exception
    }
}
