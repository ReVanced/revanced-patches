package app.revanced.patches.tiktok.feedfilter

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.feedApiServiceLIZMethod by gettingFirstMutableMethodDeclaratively {
    name("fetchFeedList")
    definingClass("/FeedApiService;")
}

internal val BytecodePatchContext.followFeedMethod by gettingFirstMutableMethodDeclaratively("getFollowFeedList") {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Lcom/ss/android/ugc/aweme/follow/presenter/FollowFeedList;")
    opcodes(
        Opcode.INVOKE_INTERFACE_RANGE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_INTERFACE
    )
}