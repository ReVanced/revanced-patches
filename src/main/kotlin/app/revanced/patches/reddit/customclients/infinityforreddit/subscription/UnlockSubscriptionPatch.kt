package app.revanced.patches.reddit.customclients.infinityforreddit.subscription

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.customclients.infinityforreddit.api.SpoofClientPatch
import app.revanced.patches.reddit.customclients.infinityforreddit.subscription.fingerprints.BillingClientOnServiceConnected
import app.revanced.patches.reddit.customclients.infinityforreddit.subscription.fingerprints.StartSubscriptionActivityFingerprint
import app.revanced.util.returnEarly

@Patch(
    name = "Unlock subscription",
    description = "Unlocks the subscription feature but requires a custom client ID.",
    compatiblePackages = [
        CompatiblePackage("ml.docilealligator.infinityforreddit"),
    ],
    dependencies = [SpoofClientPatch::class],
)
@Suppress("unused")
object UnlockSubscriptionPatch : BytecodePatch(
    setOf(StartSubscriptionActivityFingerprint, BillingClientOnServiceConnected),
) {
    override fun execute(context: BytecodeContext) =
        listOf(StartSubscriptionActivityFingerprint, BillingClientOnServiceConnected).returnEarly()
}
