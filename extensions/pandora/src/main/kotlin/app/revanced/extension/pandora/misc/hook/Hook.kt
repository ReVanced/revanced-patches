package app.revanced.extension.pandora.misc.hook

interface Hook<T> {
    /**
     * Hook the given type.
     * @param type The type to hook
     */
    fun hook(type: T): T
}
