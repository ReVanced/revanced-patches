package app.revanced.patches.twitter.layout.hideviews.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object InlineActionTypesFingerprint : MethodFingerprint(
    returnType = "Ljava/util/List",
    access = AccessFlags.PUBLIC or AccessFlags.STATIC,
    strings = listOf(
        "getCurrentMemoizing()",
        "android_animated_reply_icon_enabled",
        "reply_voting_android_position_before_favorite_enabled"
    )
)