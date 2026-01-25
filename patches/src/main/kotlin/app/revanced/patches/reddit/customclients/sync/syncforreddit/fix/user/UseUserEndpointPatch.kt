package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.user

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.extensions.stringReference
import app.revanced.patcher.patch.creatingBytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused", "ObjectPropertyName")
val `Use /user/ endpoint` by creatingBytecodePatch(
    description = "Replaces the deprecated endpoint for viewing user profiles /u with /user, that used to fix a bug.",
    use = false,

) {
    compatibleWith(
        "com.laurencedawson.reddit_sync",
        "com.laurencedawson.reddit_sync.pro",
        "com.laurencedawson.reddit_sync.dev",
    )

    apply {
        arrayOf(
            oAuthFriendRequestMethodMatch,
            oAuthSubredditInfoRequestConstructorMethodMatch,
            oAuthSubredditInfoRequestHelperMethodMatch,
            oAuthUnfriendRequestMethodMatch,
            oAuthUserIdRequestMethodMatch,
            oAuthUserInfoRequestMethodMatch,
        ).map { match ->
            match.indices.first() to match.method
        }.forEach { (userPathStringIndex, method) ->
            val userPathStringInstruction = method.getInstruction<OneRegisterInstruction>(userPathStringIndex)

            val userPathStringRegister = userPathStringInstruction.registerA
            val fixedUserPathString = userPathStringInstruction.stringReference!!.string.replace("u/", "user/")

            method.replaceInstruction(
                userPathStringIndex,
                "const-string v$userPathStringRegister, \"${fixedUserPathString}\"",
            )
        }
    }
}
