package app.revanced.patches.googlephotos.preferences.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object BackupPreferencesFingerprint : MethodFingerprint(
    returnType = "Lcom/google/android/apps/photos/backup/data/BackupPreferences;",
    strings = listOf(
        "backup_prefs_had_backup_only_when_charging_enabled",
    ),
)
