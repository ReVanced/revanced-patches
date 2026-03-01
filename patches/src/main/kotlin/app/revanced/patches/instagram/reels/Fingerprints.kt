package app.revanced.patches.instagram.reels

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.clipsViewPagerImplGetViewAtIndexMethod by gettingFirstMethodDeclaratively("ClipsViewPagerImpl_getViewAtIndex")

internal val BytecodePatchContext.clipsSwipeRefreshLayoutOnInterceptTouchEventMethod by gettingFirstMethodDeclaratively {
    parameterTypes("Landroid/view/MotionEvent;")
    definingClass("Linstagram/features/clips/viewer/ui/ClipsSwipeRefreshLayout;")
}

