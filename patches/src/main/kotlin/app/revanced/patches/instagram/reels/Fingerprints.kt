package app.revanced.patches.instagram.reels

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.clipsViewPagerImplGetViewAtIndexMethod by gettingFirstMutableMethodDeclaratively("ClipsViewPagerImpl_getViewAtIndex")

internal val BytecodePatchContext.clipsSwipeRefreshLayoutOnInterceptTouchEventMethod by gettingFirstMutableMethodDeclaratively {
    parameterTypes("Landroid/view/MotionEvent;")
    definingClass("Linstagram/features/clips/viewer/ui/ClipsSwipeRefreshLayout;")
}

