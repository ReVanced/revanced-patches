package app.revanced.patches.openinghours.misc.fix.crash.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object SetPlaceFingerprint : MethodFingerprint(
    "V",
    parameters = listOf("Lde/simon/openinghours/models/Place;"),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass == "Lde/simon/openinghours/views/custom/PlaceCard;" &&
            methodDef.name == "setPlace"
    },
)
