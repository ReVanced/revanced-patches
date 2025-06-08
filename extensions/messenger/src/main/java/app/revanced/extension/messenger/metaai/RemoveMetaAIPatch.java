package app.revanced.extension.messenger.metaai;

@SuppressWarnings("unused")
public class RemoveMetaAIPatch {
    public static boolean overrideBooleanFlag(long id, boolean value) {
        // A list of specific flags that need disabling would probably be better,
        // but these IDs change slightly with each app update.
        if ((id & 0x7FFFFFC000000000L) == 0x810a8000000000L)
            return false;

        return value;
    }
}
