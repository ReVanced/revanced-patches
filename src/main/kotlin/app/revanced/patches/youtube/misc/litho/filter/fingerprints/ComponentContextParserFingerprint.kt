package app.revanced.patches.youtube.misc.litho.filter.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val componentContextParserFingerprint = methodFingerprint {
    strings("Component was not found %s because it was removed due to duplicate converter bindings.")
}
