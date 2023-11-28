package app.revanced.patches.twitter.misc.links.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// Returns a shareable link string based on a tweet ID and a username
object LinkBuilderMethodFingerprint : MethodFingerprint(
    strings = listOf("/%1\$s/status/%2\$d"),
)