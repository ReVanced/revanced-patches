package app.revanced.patches.openinghours.misc.fix.crash.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val setPlaceFingerprint = methodFingerprint {
    returns("V")
    parameters("Lde/simon/openinghours/models/Place;")
    custom { methodDef, _ ->
        methodDef.definingClass == "Lde/simon/openinghours/views/custom/PlaceCard;" &&
                methodDef.name == "setPlace"
    }
}
