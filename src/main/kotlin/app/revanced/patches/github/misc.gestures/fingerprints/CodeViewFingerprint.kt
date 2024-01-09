package app.revanced.patches.github.misc.gestures.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object CodeViewFingerprint : MethodFingerprint(
    parameters = listOf("Landroid/view/ViewGroup;", "Landroidx/recyclerview/widget/RecyclerView;")
)