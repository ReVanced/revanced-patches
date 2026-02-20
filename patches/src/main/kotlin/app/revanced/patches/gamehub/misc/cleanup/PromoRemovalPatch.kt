package app.revanced.patches.gamehub.misc.cleanup

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.returnEarly
import org.w3c.dom.Element

private val hideCommunityBannerPatch = resourcePatch {
    execute {
        document("res/layout/llauncher_view_my_top_platform_pc_emulator_info.xml").use { dom ->
            (dom.documentElement as Element).apply {
                setAttribute("android:visibility", "gone")
                setAttribute("android:layout_height", "0dp")
                removeAttribute("android:paddingBottom")
            }
        }
    }
}

@Suppress("unused")
val popupRemovalPatch = bytecodePatch(
    name = "Remove promotional materials",
    description = "Removes promotional popup dialogs and the join community banner.",
) {
    compatibleWith("com.xiaoji.egggame"("5.3.5"))

    dependsOn(hideCommunityBannerPatch)

    execute {
        promotionalDialogFingerprint.method.returnEarly()
    }
}
