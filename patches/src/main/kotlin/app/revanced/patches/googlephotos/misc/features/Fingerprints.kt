package app.revanced.patches.googlephotos.misc.features

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.initializeFeaturesEnumMethod by gettingFirstMutableMethodDeclaratively("com.google.android.apps.photos.NEXUS_PRELOAD")