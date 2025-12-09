package app.revanced.patches.instagram.reels

import app.revanced.patcher.fingerprint

internal val clipsViewPagerImplGetViewAtIndexFingerprint = fingerprint {
    strings("ClipsViewPagerImpl_getViewAtIndex")
}

internal val clipsSwipeRefreshLayoutOnInterceptTouchEventFingerprint  = fingerprint {
    parameters("Landroid/view/MotionEvent;")
    custom { _, classDef -> classDef.type == "Linstagram/features/clips/viewer/ui/ClipsSwipeRefreshLayout;" }
}

