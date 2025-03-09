package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.fingerprint

internal val getAppSignatureFingerprint = fingerprint { strings("Failed to get the application signatures") }
