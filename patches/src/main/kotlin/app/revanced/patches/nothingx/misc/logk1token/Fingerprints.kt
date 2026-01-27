package app.revanced.patches.nothingx.misc.logk1token

import app.revanced.patcher.fingerprint

/**
 * Fingerprint for the Application onCreate method.
 * This is used to trigger scanning for existing log files on app startup.
 */
internal val applicationOnCreateFingerprint = fingerprint {
    returns("V")
    parameters()
    custom { method, classDef ->
        // Match BaseApplication onCreate specifically
        method.name == "onCreate" && classDef.endsWith("BaseApplication;")
    }
}
