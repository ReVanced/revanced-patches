package app.revanced.patches.duolingo.debug.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * The `BuildConfigProvider` class has two booleans:
 *
 * - `isChina`: (usually) compares "play" with "china"...except for builds in China
 * - `isDebug`: compares "release" with "debug" <-- we want to force this to `true`
 */
internal object InitializeBuildConfigProviderFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    strings = listOf(
        "debug",
        "release",
        "china",
    ),
)