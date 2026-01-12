package app.revanced.patches.reddit.customclients.infinityforreddit.subscription

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.reddit.customclients.infinityforreddit.api.spoofClientPatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Unlock subscription` by creatingBytecodePatch(
    description = "Unlocks the subscription feature but requires a custom client ID.",
) {
    dependsOn(spoofClientPatch)

    compatibleWith(
        "ml.docilealligator.infinityforreddit",
        "ml.docilealligator.infinityforreddit.plus",
        "ml.docilealligator.infinityforreddit.patreon"
    )

    apply {
        setOf(
            billingClientOnServiceConnectedMethod,
            startSubscriptionActivityMethod,
        ).forEach { it.returnEarly() }
    }
}
