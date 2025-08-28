package app.revanced.patches.reddit.customclients.boostforreddit.fix.redgifs

import app.revanced.patcher.fingerprint

internal val createOkHttpClientFingerprint = fingerprint {
    custom { method, _ -> method.name == "j" && method.definingClass == "Lfc/a;" }
}
