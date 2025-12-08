package app.revanced.patches.reddit.ad.comments

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

internal val hideCommentAdsFingerprint = fingerprint {
    strings("PdpCommentsAds(adPosts=")
}
