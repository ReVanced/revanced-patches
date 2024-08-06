package app.revanced.patches.shared.misc.gms.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object GooglePlayUtilityFingerprint : MethodFingerprint(
    returnType = "I",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    parameters = listOf("L", "I"),
    strings = listOf("This should never happen.", "MetadataValueReader", "com.google.android.gms"),
)
