package app.revanced.patches.tumblr.live.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

// This works identically to the Tumblr AdWaterfallFingerprint, see comments there
object LiveMarqueeFingerprint : MethodFingerprint(strings = listOf("live_marquee"))