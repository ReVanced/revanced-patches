package app.revanced.extension.messenger.metaai;

@SuppressWarnings("unused")
public class RemoveMetaAIPatch {
    public static boolean overrideBooleanFlag(long id, boolean value) {
        // This catches all flag IDs related to Meta AI.
        // The IDs change slightly with every update,
        // so to work around this, IDs from different versions were compared
        // to find what they have in common, which turned out to be those first bits.
        // TODO: Find the specific flags that we care about and patch the code they control instead.
        if ((id & 0x7FFFFFC000000000L) == 0x810A8000000000L) {
            return false;
        }

        return value;
    }
}
