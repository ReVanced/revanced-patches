package app.revanced.extension.messenger.metaai;

@SuppressWarnings("unused")
public class RemoveMetaAIPatch {
    public static boolean overrideConfigBool(long id, boolean value) {
        // it seems like all configs starting with 363219 are related to Meta AI
        // a list of specific ones that need disabling would probably be better,
        // but these config numbers seem to change slightly with each update
        // the starting digits don't, and seems to work perfectly fine
        if (String.valueOf(id).startsWith("363219"))
            return false;

        return value;
    }
}
