package app.revanced.patches.googlephotos.misc.preferences

import app.revanced.patcher.fingerprint

internal val backupPreferencesFingerprint = fingerprint {
    returns("Lcom/google/android/apps/photos/backup/data/BackupPreferences;")
    strings("backup_prefs_had_backup_only_when_charging_enabled")
}
