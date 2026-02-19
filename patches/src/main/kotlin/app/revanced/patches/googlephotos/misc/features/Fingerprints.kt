package app.revanced.patches.googlephotos.misc.features

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.initializeFeaturesEnumMethod by gettingFirstMethodDeclaratively("com.google.android.apps.photos.NEXUS_PRELOAD")