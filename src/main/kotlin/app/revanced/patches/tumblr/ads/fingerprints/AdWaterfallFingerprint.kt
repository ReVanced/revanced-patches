package app.revanced.patches.tumblr.ads.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

// The Tumblr app sends a request to /v2/timeline/dashboard which replies with an array of elements
// to show in the user dashboard. These element have different type ids (post, title, carousel, etc.)
// The standard dashboard Ad has the id client_side_ad_waterfall, and this string has to be in the code
// to handle ads and provide their appearance.
// If we just replace this string in the tumblr code with anything else, it will fail to recognize the
// dashboard object type and just skip it. This is a bit weird, but it shouldn't break
// unless they change the api (unlikely) or explicitly Change the tumblr code to prevent this.
object AdWaterfallFingerprint : MethodFingerprint(strings = listOf("client_side_ad_waterfall"))