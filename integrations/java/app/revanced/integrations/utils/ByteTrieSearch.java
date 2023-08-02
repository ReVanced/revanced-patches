package app.revanced.integrations.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public final class ByteTrieSearch extends TrieSearch<byte[]> {

    private static final class ByteTrieNode extends TrieNode<byte[]> {
        TrieNode<byte[]> createNode() {
            return new ByteTrieNode();
        }
        char getCharValue(byte[] text, int index) {
            return (char) text[index];
        }
    }

    public ByteTrieSearch() {
        super(new ByteTrieNode());
    }

    @Override
    public void addPattern(@NonNull byte[] pattern) {
        super.addPattern(pattern, pattern.length, null);
    }

    @Override
    public void addPattern(@NonNull byte[] pattern, @NonNull TriePatternMatchedCallback<byte[]> callback) {
        super.addPattern(pattern, pattern.length, Objects.requireNonNull(callback));
    }

    @Override
    public boolean matches(@NonNull byte[] textToSearch, @Nullable Object callbackParameter) {
        return super.matches(textToSearch, textToSearch.length, callbackParameter);
    }

}
