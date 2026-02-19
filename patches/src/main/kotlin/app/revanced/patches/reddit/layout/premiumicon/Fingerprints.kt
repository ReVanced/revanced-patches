package app.revanced.patches.reddit.layout.premiumicon

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.hasPremiumIconAccessMethod by gettingFirstMethodDeclaratively {
    name("isPremiumSubscriber")
    definingClass { endsWith("MyAccount;") }
    returnType("Z")
}
