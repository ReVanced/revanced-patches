package app.revanced.patches.music.misc.gms

import app.revanced.patches.music.misc.gms.Constants.MUSIC_PACKAGE_NAME
import app.revanced.patches.music.misc.gms.Constants.REVANCED_MUSIC_PACKAGE_NAME
import app.revanced.patches.shared.misc.gms.AbstractGmsCoreSupportResourcePatch

object GmsCoreSupportResourcePatch : AbstractGmsCoreSupportResourcePatch(
    fromPackageName = MUSIC_PACKAGE_NAME,
    toPackageName = REVANCED_MUSIC_PACKAGE_NAME,
    spoofedPackageSignature = "afb0fed5eeaebdd86f56a97742f4b6b33ef59875"
)