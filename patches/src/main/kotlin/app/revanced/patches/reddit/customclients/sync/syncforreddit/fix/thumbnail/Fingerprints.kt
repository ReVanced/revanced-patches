package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.thumbnail

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val customImageViewLoadFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    parameters("Ljava/lang/String;", "Z", "Z", "I", "I")
    custom { _, classDef ->
        classDef.endsWith("CustomImageView;")
    }
}
