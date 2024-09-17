package app.revanced.patches.reddit.customclients.syncforreddit.fix.user.fingerprints

import app.revanced.patcher.extensions.or
import com.android.tools.smali.dexlib2.AccessFlags

internal object OAuthSubredditInfoRequestConstructorFingerprint :
    BaseUserEndpointFingerprint(
        "OAuthSubredditInfoRequest.java",
        AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    )
