package app.revanced.patches.reddit.customclients.infinityforreddit.subscription

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.reddit.customclients.infinityforreddit.api.spoofClientPatch
import app.revanced.util.returnEarly

@Suppress("unused")
val unlockSubscriptionPatch = bytecodePatch(
    name = "Unlock subscription",
    description = "Unlocks the subscription feature but requires a custom client ID.",
) {
    dependsOn(spoofClientPatch)

    compatibleWith("ml.docilealligator.infinityforreddit")

    execute {
        setOf(
            startSubscriptionActivityFingerprint,
            billingClientOnServiceConnectedFingerprint,
        ).forEach { it.method().returnEarly() }
    }
}
