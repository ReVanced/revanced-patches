package app.revanced.patches.gamehub.misc.extension

import app.revanced.patcher.patch.bytecodePatch

/**
 * Loads the gamehub extension into the target APK.
 * Must be a dependency of any patch that references extension classes.
 */
internal val sharedGamehubExtensionPatch = bytecodePatch {
    extendWith("extensions/gamehub.rve")
}
