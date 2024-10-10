package app.revanced.patches.tumblr.annoyances.notifications

import app.revanced.patcher.fingerprint

// The BlogNotifyCtaDialog asks you if you want to enable notifications for a blog.
// It shows whenever you visit a certain blog for the second time and disables itself
// if it was shown a total of 3 times (stored in app storage).
// This targets the BlogNotifyCtaDialog.isEnabled() method to let it always return false.
internal val isBlogNotifyEnabledFingerprint = fingerprint {
    strings("isEnabled --> ", "blog_notify_enabled")
}
