package app.revanced.patches.tasker.trial.unlock.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("net.dinglisch.android.taskerm")])
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class UnlockTrialCompatibility
