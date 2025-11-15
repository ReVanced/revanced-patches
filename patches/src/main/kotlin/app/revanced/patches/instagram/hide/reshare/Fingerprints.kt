package app.revanced.patches.instagram.hide.reshare

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val mediaJsonParserFingerprint = fingerprint {
    strings("share_count_disabled", "MediaDict")
    custom { method, _ -> method.name == "parseFromJson" }
}
