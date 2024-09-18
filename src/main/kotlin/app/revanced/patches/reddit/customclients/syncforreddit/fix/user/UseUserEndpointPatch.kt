package app.revanced.patches.reddit.customclients.syncforreddit.fix.user

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.customclients.syncforreddit.fix.user.fingerprints.*
import app.revanced.patches.reddit.customclients.syncforreddit.fix.user.fingerprints.OAuthFriendRequestFingerprint
import app.revanced.patches.reddit.customclients.syncforreddit.fix.user.fingerprints.OAuthSubredditInfoRequestHelperFingerprint
import app.revanced.patches.reddit.customclients.syncforreddit.fix.user.fingerprints.OAuthUnfriendRequestFingerprint
import app.revanced.util.getReference
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Patch(
    name = "Use /user/ endpoint",
    description = "Replaces the deprecated endpoint for viewing user profiles /u with /user, that used to fix a bug.",
    compatiblePackages = [
        CompatiblePackage("com.laurencedawson.reddit_sync"),
        CompatiblePackage("com.laurencedawson.reddit_sync.pro"),
        CompatiblePackage("com.laurencedawson.reddit_sync.dev"),
    ],
    use = false,
)
@Suppress("unused")
object UseUserEndpointPatch : BytecodePatch(
    fingerprints = setOf(
        OAuthFriendRequestFingerprint,
        OAuthSubredditInfoRequestConstructorFingerprint,
        OAuthSubredditInfoRequestHelperFingerprint,
        OAuthUnfriendRequestFingerprint,
        OAuthUserIdRequestFingerprint,
        OAuthUserInfoRequestFingerprint,
    ),
) {
    override fun execute(context: BytecodeContext) {
        arrayOf(
            OAuthFriendRequestFingerprint,
            OAuthSubredditInfoRequestConstructorFingerprint,
            OAuthSubredditInfoRequestHelperFingerprint,
            OAuthUnfriendRequestFingerprint,
            OAuthUserIdRequestFingerprint,
            OAuthUserInfoRequestFingerprint,
        ).map(MethodFingerprint::resultOrThrow).map {
            it.scanResult.stringsScanResult!!.matches.first().index to it.mutableMethod
        }.forEach { (userPathStringIndex, method) ->
            val userPathStringInstruction = method.getInstruction<OneRegisterInstruction>(userPathStringIndex)
            val userPathStringRegister = userPathStringInstruction.registerA
            val fixedUserPathString = userPathStringInstruction.getReference<StringReference>()!!.string.replace("u/", "user/")

            method.replaceInstruction(
                userPathStringIndex,
                "const-string v$userPathStringRegister, \"${fixedUserPathString}\"",
            )
        }
    }
}
