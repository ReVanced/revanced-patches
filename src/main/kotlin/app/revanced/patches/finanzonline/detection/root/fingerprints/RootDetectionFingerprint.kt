package app.revanced.patches.finanzonline.detection.root.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object RootDetectionFingerprint : MethodFingerprint(
    "L",
    customFingerprint = { methodDef ->
        methodDef.definingClass == "Lat/gv/bmf/bmf2go/tools/utils/z;"
    }
)
