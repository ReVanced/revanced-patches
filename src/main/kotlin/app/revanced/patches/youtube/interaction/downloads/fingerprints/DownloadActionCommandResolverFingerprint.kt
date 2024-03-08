package app.revanced.patches.youtube.interaction.downloads.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Resolves to the class found in [DownloadActionCommandResolverParentFingerprint].
 */
internal object DownloadActionCommandResolverFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "V",
    parameters = listOf("L", "Ljava/util/Map;")
)
