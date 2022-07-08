package app.revanced.patches.youtube.interaction.fenster.patch

import app.revanced.extensions.injectConsumableEventHook
import app.revanced.extensions.injectIntoNamedMethod
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.youtube.interaction.fenster.annotation.FensterCompatibility
import app.revanced.patches.youtube.interaction.fenster.fingerprints.UpdatePlayerTypeFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference

@Patch
@Name("fenster-swipe-controls")
@Description("Adds volume and brightness swipe controls.")
@FensterCompatibility
@Version("0.0.1")
@Dependencies(dependencies = [IntegrationsPatch::class])
class FensterPatch : BytecodePatch(
    listOf(
        UpdatePlayerTypeFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        // hook WatchWhileActivity.onStart (main activity lifecycle hook)
        data.injectIntoNamedMethod(
            "com/google/android/apps/youtube/app/watchwhile/WatchWhileActivity",
            "onStart",
            0,
            "invoke-static { p0 }, Lapp/revanced/integrations/patches/FensterSwipePatch;->WatchWhileActivity_onStartHookEX(Ljava/lang/Object;)V"
        )

        // hook YoutubePlayerOverlaysLayout.onFinishInflate (player overlays init hook)
        data.injectIntoNamedMethod(
            "com/google/android/apps/youtube/app/common/player/overlay/YouTubePlayerOverlaysLayout",
            "onFinishInflate",
            -2,
            "invoke-static { p0 }, Lapp/revanced/integrations/patches/FensterSwipePatch;->YouTubePlayerOverlaysLayout_onFinishInflateHookEX(Ljava/lang/Object;)V"
        )

        // hook YoutubePlayerOverlaysLayout.UpdatePlayerType
        injectUpdatePlayerTypeHook(
            UpdatePlayerTypeFingerprint.result!!,
            "com/google/android/apps/youtube/app/common/player/overlay/YouTubePlayerOverlaysLayout"
        )

        // hook NextGenWatchLayout.onTouchEvent and NextGenWatchLayout.onInterceptTouchEvent (player touch event hook)
        injectWatchLayoutTouchHooks(
            data,
            "com/google/android/apps/youtube/app/watch/nextgenwatch/ui/NextGenWatchLayout"
        )

        return PatchResultSuccess()
    }

    @Suppress("SameParameterValue")
    private fun injectUpdatePlayerTypeHook(fingerPrintResult: MethodFingerprintResult, targetClass: String) {
        // validate fingerprint found the right class
        if (!fingerPrintResult.classDef.type.endsWith("$targetClass;")) {
            throw PatchResultError("$targetClass.UpdatePlayerType fingerprint could not be validated")
        }

        // insert the hook
        fingerPrintResult.mutableMethod.addInstruction(
            0,
            "invoke-static { p1 }, Lapp/revanced/integrations/patches/FensterSwipePatch;->YouTubePlayerOverlaysLayout_updatePlayerTypeHookEX(Ljava/lang/Object;)V"
        )
    }

    /**
     * Inject onTouch event hooks into the watch layout class
     *
     * @param data bytecode data
     * @param targetClass watch layout class name
     */
    @Suppress("SameParameterValue")
    private fun injectWatchLayoutTouchHooks(data: BytecodeData, targetClass: String) {
        var touchHooksCount = 0
        data.classes.filter { it.type.endsWith("$targetClass;") }.forEach { classDef ->
            // hook onTouchEvent
            data.proxy(classDef).resolve().methods.filter { it.name == "onTouchEvent" }.forEach { methodDef ->
                touchHooksCount++
                methodDef.injectConsumableEventHook(
                    ImmutableMethodReference(
                        "Lapp/revanced/integrations/patches/FensterSwipePatch;",
                        "NextGenWatchLayout_onTouchEventHookEX",
                        listOf("Ljava/lang/Object;", "Ljava/lang/Object;"),
                        "Z"
                    )
                )
            }

            // hook onInterceptTouchEvent
            data.proxy(classDef).resolve().methods.filter { it.name == "onInterceptTouchEvent" }.forEach { methodDef ->
                touchHooksCount++
                methodDef.injectConsumableEventHook(
                    ImmutableMethodReference(
                        "Lapp/revanced/integrations/patches/FensterSwipePatch;",
                        "NextGenWatchLayout_onInterceptTouchEventHookEX",
                        listOf("Ljava/lang/Object;", "Ljava/lang/Object;"),
                        "Z"
                    )
                )
            }
        }

        // fail if no touch hooks were inserted
        if (touchHooksCount <= 0) {
            throw PatchResultError("failed to inject onTouchEvent hook into NextGenWatchLayout: none found")
        }
    }
}
