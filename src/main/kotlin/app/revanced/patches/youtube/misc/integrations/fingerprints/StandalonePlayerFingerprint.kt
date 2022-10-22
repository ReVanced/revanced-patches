package app.revanced.patches.youtube.misc.integrations.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.integrations.annotations.IntegrationsCompatibility

@Name("standalone-player-fingerprint")
@IntegrationsCompatibility
@Version("0.0.1")
object StandalonePlayerFingerprint : MethodFingerprint(
    strings = listOf(
        "Invalid PlaybackStartDescriptor. Returning the instance itself.",
        "com.google.android.music",
    )
)