package app.revanced.patches.tumblr.timelinefilter.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// This is the constructor of the PostsResponse class.
// The same applies here as with the TimelineConstructorFingerprint.
internal val postsResponseConstructorFingerprint = methodFingerprint {
    accessFlags(AccessFlags.CONSTRUCTOR, AccessFlags.PUBLIC)
    custom { methodDef, _ -> methodDef.definingClass.endsWith("/PostsResponse;") && methodDef.parameters.size == 4 }
}
