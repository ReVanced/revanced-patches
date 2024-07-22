package app.revanced.patches.all.privacy.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object DoConfigFetchFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC.value,
    customFingerprint = { methodDef, classDef ->
        classDef.sourceFile == "RemoteSettingsFetcher.kt" && methodDef.name == "doConfigFetch"
    }
)