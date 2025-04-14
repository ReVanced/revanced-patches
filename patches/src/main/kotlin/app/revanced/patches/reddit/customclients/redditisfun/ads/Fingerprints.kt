package app.revanced.patches.reddit.customclients.redditisfun.ads

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// Should usually match: o5/i0.a()
internal val userPremiumFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Z")
    parameters()
    custom { _, classDef ->
        // Expect the class to have exactly one static field of type HashSet.
        val sfIter = classDef.staticFields.iterator()
        sfIter.hasNext()
                && sfIter.next().type == "Ljava/util/HashSet;"
                && !sfIter.hasNext()
    }
}
