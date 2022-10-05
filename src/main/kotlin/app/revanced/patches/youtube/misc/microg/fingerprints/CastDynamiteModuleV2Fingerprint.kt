package app.revanced.patches.youtube.misc.microg.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.microg.annotations.MicroGPatchCompatibility

@Name("cast-context-fetch-fingerprint")
@MicroGPatchCompatibility
@Version("0.0.1")
object CastDynamiteModuleV2Fingerprint : MethodFingerprint(
    strings = listOf("Failed to load module via V2: ")
)