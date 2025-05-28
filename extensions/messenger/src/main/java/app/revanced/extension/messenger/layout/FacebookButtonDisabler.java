package app.revanced.extension.messenger.layout;

import app.revanced.extension.messenger.config.AppFeatureFlagsOverrider;

@SuppressWarnings("unused")
public final class FacebookButtonDisabler implements AppFeatureFlagsOverrider {
    @Override
    public Boolean overrideBooleanFlag(long id, boolean value) {
        // The specific config id for version 510.0.0.0.15 is 72341384002083474L
        // Unfortunately, these ids are not the same across app versions,
        // so we mask out the bits that seem to change, and use that.
        // This unfortunately makes it also disable around 2 other configs,
        // but the effects of that have not been noticed yet.
        if ((id & 0xffffff0ffffff00L) == 0x101021000011a00L)
            return Boolean.FALSE;

        return null;
    }
}
