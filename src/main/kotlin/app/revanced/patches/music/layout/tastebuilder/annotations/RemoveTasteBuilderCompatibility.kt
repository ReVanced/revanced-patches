package app.revanced.patches.music.layout.tastebuilder.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

/**
 * -- Note 2022-08-05 --
 * Since 5.17.xx the tastebuilder component is dismissible, so this patch is less useful
 * also it is partly litho now
 */
@Compatibility([Package("com.google.android.apps.youtube.music")])
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class RemoveTasteBuilderCompatibility
