package app.revanced.twitter.patches.hook.patch

interface Hook<T> {
    /**
     * Hook the given type.
     * @param type The type to hook
     */
    fun hook(type: T): T
}