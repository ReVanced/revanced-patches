package app.revanced.patches.openinghours.misc.fix.crash

import app.revanced.patcher.fingerprint

internal val setPlaceFingerprint = fingerprint {
    returns("V")
    parameters("Lde/simon/openinghours/models/Place;")
    custom { method, _ ->
        method.name == "setPlace" &&
            method.definingClass == "Lde/simon/openinghours/views/custom/PlaceCard;"
    }
}
