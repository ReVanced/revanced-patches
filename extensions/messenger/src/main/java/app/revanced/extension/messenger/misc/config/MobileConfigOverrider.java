package app.revanced.extension.messenger.misc.config;

public abstract class MobileConfigOverrider {
    public boolean overrideConfigBool(long id, boolean value) {
        return value;
    }
}
