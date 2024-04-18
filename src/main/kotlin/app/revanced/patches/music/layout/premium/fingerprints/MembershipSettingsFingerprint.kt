package app.revanced.patches.music.layout.premium.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object MembershipSettingsFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/CharSequence;",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = emptyList()
)
