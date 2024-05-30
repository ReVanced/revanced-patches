package app.revanced.patches.music.misc.gms

import app.revanced.patcher.patch.Option
import app.revanced.patches.music.misc.gms.Constants.MUSIC_PACKAGE_NAME
import app.revanced.patches.music.misc.gms.Constants.REVANCED_MUSIC_PACKAGE_NAME
import app.revanced.patches.shared.misc.gms.gmsCoreSupportResourcePatch

internal fun gmsCoreSupportResourcePatch(
    gmsCoreVendorGroupIdOption: Option<String>,
) = gmsCoreSupportResourcePatch(
    fromPackageName = MUSIC_PACKAGE_NAME,
    toPackageName = REVANCED_MUSIC_PACKAGE_NAME,
    gmsCoreVendorGroupIdOption = gmsCoreVendorGroupIdOption,
    spoofedPackageSignature = "afb0fed5eeaebdd86f56a97742f4b6b33ef59875",
)
