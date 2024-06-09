package app.revanced.patches.cieid.restrictions.root.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val checkRootFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.type == "Lit/ipzs/cieid/BaseActivity;" && methodDef.name == "onResume"
    }
}