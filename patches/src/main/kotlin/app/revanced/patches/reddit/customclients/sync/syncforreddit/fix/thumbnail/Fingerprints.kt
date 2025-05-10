package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.thumbnail

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

val customImageViewLoadFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    parameters("Ljava/lang/String;", "Z", "Z", "I", "I")
    custom { _, classDef ->
        classDef.type == "Lcom/laurencedawson/reddit_sync/ui/views/images/CustomImageView;"
    }
}
