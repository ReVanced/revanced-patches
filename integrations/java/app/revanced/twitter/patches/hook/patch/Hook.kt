package app.revanced.twitter.patches.hook.patch

import androidx.annotation.NonNull

interface Hook<T> {
    /**
     * Hook the given type.
     * @param type The type to hook
     */
    fun hook(@NonNull type: T): T
}