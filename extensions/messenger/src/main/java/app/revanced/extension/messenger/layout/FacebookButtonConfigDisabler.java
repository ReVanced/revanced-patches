package app.revanced.extension.messenger.layout;

import app.revanced.extension.messenger.misc.config.MobileConfigOverrider;

@SuppressWarnings("unused")
public final class FacebookButtonConfigDisabler extends MobileConfigOverrider {
    @Override
    public boolean overrideConfigBool(long id, boolean value) {
        // This disables 2 other configs too unfortunately,
        // but this is the
        //if ((id & 0xffffff0ffffff00L) == 0x101021000011A00L)
        if (id==72341384002083474L)
            return false;

        return value;
    }
}
