package app.revanced.extension.shared;

import java.nio.charset.StandardCharsets;

public final class ByteTrieSearch extends TrieSearch<byte[]> {

    private static final class ByteTrieNode extends TrieNode<byte[]> {
        ByteTrieNode() {
            super();
        }
        ByteTrieNode(char nodeCharacterValue) {
            super(nodeCharacterValue);
        }
        @Override
        TrieNode<byte[]> createNode(char nodeCharacterValue) {
            return new ByteTrieNode(nodeCharacterValue);
        }
        @Override
        char getCharValue(byte[] text, int index) {
            return (char) text[index];
        }
        @Override
        int getTextLength(byte[] text) {
            return text.length;
        }
    }

    /**
     * Helper method for the common usage of converting Strings to raw UTF-8 bytes.
     */
    public static byte[][] convertStringsToBytes(String... strings) {
        final int length = strings.length;
        byte[][] replacement = new byte[length][];
        for (int i = 0; i < length; i++) {
            replacement[i] = strings[i].getBytes(StandardCharsets.UTF_8);
        }
        return replacement;
    }

    public ByteTrieSearch(byte[]... patterns) {
        super(new ByteTrieNode(), patterns);
    }
}
