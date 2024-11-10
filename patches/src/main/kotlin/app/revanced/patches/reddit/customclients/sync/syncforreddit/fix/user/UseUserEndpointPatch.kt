package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.user

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Suppress("unused")
val useUserEndpointPatch = bytecodePatch(
    name = "Use /user/ endpoint",
    description = "Replaces the deprecated endpoint for viewing user profiles /u with /user, that used to fix a bug.",
    use = false,

) {
    compatibleWith(
        "com.laurencedawson.reddit_sync",
        "com.laurencedawson.reddit_sync.pro",
        "com.laurencedawson.reddit_sync.dev",
    )

    execute {
        arrayOf(
            oAuthFriendRequestFingerprint,
            oAuthSubredditInfoRequestConstructorFingerprint,
            oAuthSubredditInfoRequestHelperFingerprint,
            oAuthUnfriendRequestFingerprint,
            oAuthUserIdRequestFingerprint,
            oAuthUserInfoRequestFingerprint,
        ).map { fingerprint ->
            fingerprint.stringMatches!!.first().index to fingerprint.method
        }.forEach { (userPathStringIndex, method) ->
            val userPathStringInstruction = method.getInstruction<OneRegisterInstruction>(userPathStringIndex)

            val userPathStringRegister = userPathStringInstruction.registerA
            val fixedUserPathString = userPathStringInstruction.getReference<StringReference>()!!
                .string.replace("u/", "user/")

            method.replaceInstruction(
                userPathStringIndex,
                "const-string v$userPathStringRegister, \"${fixedUserPathString}\"",
            )
        }
    }
}
