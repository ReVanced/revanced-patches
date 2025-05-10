package app.revanced.patches.tiktok.feedfilter

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val feedApiServiceLIZFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/FeedApiService;") && method.name == "fetchFeedList"
    }
}

internal val getFollowFeedFingerprint = fingerprint {
    returns("Lcom/ss/android/ugc/aweme/follow/presenter/FollowFeedList;")
    opcodes(
        Opcode.INVOKE_INTERFACE_RANGE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_INTERFACE
    )
}
