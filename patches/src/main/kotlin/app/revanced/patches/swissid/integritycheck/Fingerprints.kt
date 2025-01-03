package app.revanced.patches.swissid.integritycheck

import app.revanced.patcher.fingerprint

internal val checkIntegrityFingerprint by fingerprint {
    returns("V")
    parameters("Lcom/swisssign/deviceintegrity/model/DeviceIntegrityResult;")
    strings("it", "result")
}
