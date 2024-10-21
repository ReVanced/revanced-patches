package app.revanced.patches.twitter.misc.links.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.twitter.misc.links.ChangeLinkSharingDomainResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// Gets Resource string for share link view available by pressing "Share via" button.
internal object LinkResourceGetterFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("Landroid/content/res/Resources;"),
    literalSupplier = { ChangeLinkSharingDomainResourcePatch.tweetShareLinkTemplateId }
)