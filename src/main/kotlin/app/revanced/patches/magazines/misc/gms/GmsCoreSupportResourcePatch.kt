package app.revanced.patches.magazines.misc.gms

import app.revanced.patches.shared.misc.gms.BaseGmsCoreSupportResourcePatch
import app.revanced.patches.magazines.misc.gms.Constants.MAGAZINES_PACKAGE_NAME
import app.revanced.patches.magazines.misc.gms.Constants.REVANCED_MAGAZINES_PACKAGE_NAME

object GmsCoreSupportResourcePatch : BaseGmsCoreSupportResourcePatch(
    fromPackageName = MAGAZINES_PACKAGE_NAME,
    toPackageName = REVANCED_MAGAZINES_PACKAGE_NAME,
    spoofedPackageSignature = "24bb24c05e47e0aefa68a58a766179d9b613a666",
)
