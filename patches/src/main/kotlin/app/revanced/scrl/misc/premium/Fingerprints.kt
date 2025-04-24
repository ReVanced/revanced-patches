package app.revanced.patches.scrl.misc.premium

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val isPremiumVersionFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters("Landroid/content/Context;")
    custom { _, classDef ->
        classDef.type == "Lcom/appostrophe/scrl/core/UpgradeManager\$Companion;"
    }
}