package app.revanced.extension.shared;

/**
 * Text pattern searching using a prefix tree (trie).
 */
public final class StringTrieSearch extends TrieSearch<String> {

    private static final class StringTrieNode extends TrieNode<String> {
        StringTrieNode() {
            super();
        }
        StringTrieNode(char nodeCharacterValue) {
            super(nodeCharacterValue);
        }
        @Override
        TrieNode<String> createNode(char nodeValue) {
            return new StringTrieNode(nodeValue);
        }
        @Override
        char getCharValue(String text, int index) {
            return text.charAt(index);
        }
        @Override
        int getTextLength(String text) {
            return text.length();
        }
    }

    public StringTrieSearch(String... patterns) {
        super(new StringTrieNode(), patterns);
    }
}
