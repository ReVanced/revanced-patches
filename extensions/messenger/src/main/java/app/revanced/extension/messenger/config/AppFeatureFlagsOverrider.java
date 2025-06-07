package app.revanced.extension.messenger.config;

public interface AppFeatureFlagsOverrider {
    Boolean overrideBooleanFlag(long id, boolean value);
}
