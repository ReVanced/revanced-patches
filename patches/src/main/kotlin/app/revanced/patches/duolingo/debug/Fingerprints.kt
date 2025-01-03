package app.revanced.patches.duolingo.debug

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * The `BuildConfigProvider` class has two booleans:
 *
 * - `isChina`: (usually) compares "play" with "china"...except for builds in China
 * - `isDebug`: compares "release" with "debug" <-- we want to force this to `true`
 */

internal val initializeBuildConfigProviderFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    opcodes(Opcode.IPUT_BOOLEAN)
    strings("debug", "release", "china")
}
