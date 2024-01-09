package app.revanced.patches.tumblr.timelinefilter.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// This is the constructor of the PostsResponse class.
// The same applies here as with the TimelineConstructorFingerprint.
internal object PostsResponseConstructorFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.CONSTRUCTOR or AccessFlags.PUBLIC,
    customFingerprint = { methodDef, _ -> methodDef.definingClass.endsWith("/PostsResponse;") && methodDef.parameters.size == 4 },
)