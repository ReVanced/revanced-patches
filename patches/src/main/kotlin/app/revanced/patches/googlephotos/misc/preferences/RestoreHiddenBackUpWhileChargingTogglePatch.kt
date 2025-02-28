package app.revanced.patches.googlephotos.misc.preferences

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val restoreHiddenBackUpWhileChargingTogglePatch = bytecodePatch(
    name = "Restore hidden 'Back up while charging' toggle",
    description = "Restores a hidden toggle to only run backups when the device is charging.",
) {
    compatibleWith("com.google.android.apps.photos")

    execute {
        // Patches 'backup_prefs_had_backup_only_when_charging_enabled' to always be true.
        val chargingPrefStringIndex = backupPreferencesFingerprint.stringMatches.first().index
        backupPreferencesFingerprint.method.apply {
            // Get the register of move-result.
            val resultRegister = getInstruction<OneRegisterInstruction>(chargingPrefStringIndex + 2).registerA
            // Insert const after move-result to override register as true.
            addInstruction(chargingPrefStringIndex + 3, "const/4 v$resultRegister, 0x1")
        }
    }
}
