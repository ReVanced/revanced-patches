package app.revanced.extension.messenger.metaai;

import app.revanced.extension.messenger.config.AppFeatureFlagsOverrider;

@SuppressWarnings("unused")
public final class MetaAIDisabler implements AppFeatureFlagsOverrider {
    @Override
    public Boolean overrideBooleanFlag(long id, boolean value) {
        // It seems like all ids starting with 363219 are related to Meta AI.
        // A list of specific ones that need disabling would probably be better,
        // but these config ids change slightly with each update.
        // These first 6 digits don't though.
        if (Long.toString(id).startsWith("363219"))
            return Boolean.FALSE;

        return null;
    }
}
