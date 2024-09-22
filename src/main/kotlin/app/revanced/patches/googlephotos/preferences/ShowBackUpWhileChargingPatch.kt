package app.revanced.patches.googlephotos.preferences

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.googlephotos.preferences.fingerprints.BackupPreferencesFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Show hidden 'Back up while charging' toggle",
    description = "This toggle is hidden by default, when enabled, backups run only when the device is charging",
    compatiblePackages = [CompatiblePackage("com.google.android.apps.photos")]
)
@Suppress("unused")
object BackupPreferencesFingerprint : BytecodePatch(
    setOf(BackupPreferencesFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        // patches 'backup_prefs_had_backup_only_when_charging_enabled' to always be true
        BackupPreferencesFingerprint.result?.let {
            val chargingPrefStringIndex = it.scanResult.stringsScanResult!!.matches.first().index    
            it.mutableMethod.apply {
                val resultRegister = getInstruction<OneRegisterInstruction>(chargingPrefStringIndex+2).registerA
                // override result as true
                addInstruction(chargingPrefStringIndex + 3, "const/4 v$resultRegister, 0x1")
            }
        } ?: throw Exception("BackupPreferencesFingerprint result not found")
    }
}
