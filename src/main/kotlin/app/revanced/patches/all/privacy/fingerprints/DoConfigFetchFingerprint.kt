package app.revanced.patches.all.privacy.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// Matches is com.google.firebase.sessions.settings.RemoteSettingsFetcher.doConfigFetch.
internal object DoConfigFetchFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC.value,
    parameters = listOf("Ljava/util/Map;", "L", "L", "L"),
    customFingerprint = { _, classDef ->
        classDef.sourceFile == "RemoteSettingsFetcher.kt"
    },
)
