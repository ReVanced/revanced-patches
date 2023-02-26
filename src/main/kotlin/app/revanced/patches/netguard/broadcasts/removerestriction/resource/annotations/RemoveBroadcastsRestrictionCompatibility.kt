package app.revanced.patches.netguard.broadcasts.removerestriction.resource.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("eu.faircode.netguard")])
@Target(AnnotationTarget.CLASS)
annotation class RemoveBroadcastsRestrictionCompatibility
