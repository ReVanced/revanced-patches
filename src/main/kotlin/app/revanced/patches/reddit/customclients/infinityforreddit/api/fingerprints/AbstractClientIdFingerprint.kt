package app.revanced.patches.reddit.customclients.infinityforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method

/**
 * Fingerprint for a method that has the client id hardcoded in it.
 * The first string in the fingerprint is the client id.
 *
 * @param customFingerprint A custom fingerprint.
 * @param additionalStrings Additional strings to add to the fingerprint.
 */
abstract class AbstractClientIdFingerprint(
    customFingerprint: ((methodDef: Method, classDef: ClassDef) -> Boolean)? = null,
    vararg additionalStrings: String
) : MethodFingerprint(strings = listOf("NOe2iKrPPzwscA", *additionalStrings), customFingerprint = customFingerprint)