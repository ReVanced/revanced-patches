package app.revanced.patches.googlephotos.misc.preferences

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Deprecated("This patch no longer works and this code will soon be deleted")
@Suppress("unused")
val restoreHiddenBackUpWhileChargingTogglePatch = bytecodePatch(
    description = "Restores a hidden toggle to only run backups when the device is charging."
) {
    compatibleWith("com.google.android.apps.photos"("7.11.0.705590205"))

    execute {
        // Patches 'backup_prefs_had_backup_only_when_charging_enabled' to always be true.
        backupPreferencesFingerprint.let {
            it.method.apply {
                val index = indexOfFirstInstructionOrThrow(
                    it.stringMatches!!.first().index,
                    Opcode.MOVE_RESULT
                )
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                addInstruction(index + 1, "const/4 v$register, 0x1")
            }
        }
    }
}
