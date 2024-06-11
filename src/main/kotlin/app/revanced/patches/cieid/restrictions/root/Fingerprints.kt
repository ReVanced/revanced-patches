package app.revanced.patches.cieid.restrictions.root

import app.revanced.patcher.fingerprint.methodFingerprint

internal val checkRootFingerprint = methodFingerprint {
    custom { method, _ ->
        method.name == "onResume" && method.definingClass == "Lit/ipzs/cieid/BaseActivity;"
    }
}
