package app.revanced.patches.tumblr.annoyances.notifications

import app.revanced.patcher.gettingFirstMethod
import app.revanced.patcher.patch.BytecodePatchContext

// The BlogNotifyCtaDialog asks you if you want to enable notifications for a blog.
// It shows whenever you visit a certain blog for the second time and disables itself
// if it was shown a total of 3 times (stored in app storage).
// This targets the BlogNotifyCtaDialog.isEnabled() method to let it always return false.
internal val BytecodePatchContext.isBlogNotifyEnabledMethod by gettingFirstMethod(
    "isEnabled --> ",
    "blog_notify_enabled",
)
