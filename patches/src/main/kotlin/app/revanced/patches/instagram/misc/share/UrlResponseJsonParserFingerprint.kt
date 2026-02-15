package app.revanced.patches.instagram.misc.share

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val TARGET_STRING_ARRAY =  arrayOf(
    "permalink",
    "story_item_to_share_url",
    "profile_to_share_url",
    "live_to_share_url",
)

internal val permalinkResponseJsonParserFingerprint = fingerprint {
    strings(TARGET_STRING_ARRAY[0])
    opcodes(Opcode.NEW_INSTANCE,Opcode.INVOKE_DIRECT,Opcode.INVOKE_VIRTUAL)
    custom { method, _ -> method.name == "parseFromJson" }
}

internal val storyUrlResponseJsonParserFingerprint = fingerprint {
    strings(TARGET_STRING_ARRAY[1])
    custom { method, _ -> method.name == "parseFromJson" }
}

internal val profileUrlResponseJsonParserFingerprint = fingerprint {
    strings(TARGET_STRING_ARRAY[2])
    custom { method, _ -> method.name == "parseFromJson" }
}

internal val liveUrlResponseJsonParserFingerprint = fingerprint {
    strings(TARGET_STRING_ARRAY[3])
    custom { method, _ -> method.name == "parseFromJson" }
}
