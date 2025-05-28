package app.revanced.extension.messenger.metaai;

import app.revanced.extension.messenger.misc.config.MobileConfigOverrider;

@SuppressWarnings("unused")
public final class MetaAIConfigDisabler extends MobileConfigOverrider {
    @Override
    public boolean overrideConfigBool(long id, boolean value) {
        // It seems like all configs starting with 363219 are related to Meta AI.
        // A list of specific ones that need disabling would probably be better,
        // but these config numbers seem to change slightly with each update.
        // These first 6 digits don't though.
        if (Long.toString(id).startsWith("363219"))
            return false;

        return value;
    }
}
