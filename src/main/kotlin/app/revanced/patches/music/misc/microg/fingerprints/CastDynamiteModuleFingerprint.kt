package app.revanced.patches.music.misc.microg.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.microg.annotations.MicroGPatchCompatibility

@Name("cast-module-fingerprint")

@MicroGPatchCompatibility
@Version("0.0.1")
object CastDynamiteModuleFingerprint : MethodFingerprint(
    strings = listOf("com.google.android.gms.cast.framework.internal.CastDynamiteModuleImpl")
)