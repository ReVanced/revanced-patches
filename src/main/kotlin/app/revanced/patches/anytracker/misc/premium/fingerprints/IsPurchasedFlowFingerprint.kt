package app.revanced.patches.anytracker.misc.premium.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

object IsPurchasedFlowFingerprint : MethodFingerprint(
    "Landroidx/lifecycle/LiveData"
    strings = listOf("premium_user", "sku"),
)
