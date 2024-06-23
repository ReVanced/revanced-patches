package app.revanced.patches.cieid.restrictions.root

import app.revanced.patcher.fingerprint

internal val checkRootFingerprint = fingerprint {
    custom { method, _ ->
        method.name == "onResume" && method.definingClass == "Lit/ipzs/cieid/BaseActivity;"
    }
}
