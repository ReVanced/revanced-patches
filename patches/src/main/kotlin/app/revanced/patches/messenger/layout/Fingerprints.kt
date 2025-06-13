package app.revanced.patches.messenger.layout

import app.revanced.patcher.fingerprint

internal val isFacebookButtonEnabledFingerprint by fingerprint {
    parameters()
    returns("Z")
    strings("com.facebook.messaging.inbox.tab.plugins.core.tabtoolbarbutton." +
            "facebookbutton.facebooktoolbarbutton.FacebookButtonTabButtonImplementation")
}
