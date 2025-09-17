package app.revanced.extension.shared;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Searches for a group of different patterns using a trie (prefix tree).
 * Can significantly speed up searching for multiple patterns.
 */
public abstract class TrieSearch<T> {

    public interface TriePatternMatchedCallback<T> {
        /**
         * Called when a pattern is matched.
         *
         * @param textSearched      Text that was searched.
         * @param matchedStartIndex Start index of the search text, where the pattern was matched.
         * @param matchedLength     Length of the match.
         * @param callbackParameter Optional parameter passed into {@link TrieSearch#matches(Object, Object)}.
         * @return True, if the search should stop here.
         * If false, searching will continue to look for other matches.
         */
        boolean patternMatched(T textSearched, int matchedStartIndex, int matchedLength, Object callbackParameter);
    }

    /**
     * Represents a compressed tree path for a single pattern that shares no sibling nodes.
     *
     * For example, if a tree contains the patterns: "foobar", "football", "feet",
     * it would contain 3 compressed paths of: "bar", "tball", "eet".
     *
     * And the tree would contain children arrays only for the first level containing 'f',
     * the second level containing 'o',
     * and the third level containing 'o'.
     *
     * This is done to reduce memory usage, which can be substantial if many long patterns are used.
     */
    private static final class TrieCompressedPath<T> {
        final T pattern;
        final int patternStartIndex;
        final int patternLength;
        final TriePatternMatchedCallback<T> callback;

        TrieCompressedPath(T pattern, int patternStartIndex, int patternLength, TriePatternMatchedCallback<T> callback) {
            this.pattern = pattern;
            this.patternStartIndex = patternStartIndex;
            this.patternLength = patternLength;
            this.callback = callback;
        }
        boolean matches(TrieNode<T> enclosingNode, // Used only for the get character method.
                        T searchText, int searchTextLength, int searchTextIndex, Object callbackParameter) {
            if (searchTextLength - searchTextIndex < patternLength - patternStartIndex) {
                return false; // Remaining search text is shorter than the remaining leaf pattern and they cannot match.
            }

            for (int i = searchTextIndex, j = patternStartIndex; j < patternLength; i++, j++) {
                if (enclosingNode.getCharValue(searchText, i) != enclosingNode.getCharValue(pattern, j)) {
                    return false;
                }
            }

            return callback == null || callback.patternMatched(searchText,
                    searchTextIndex - patternStartIndex, patternLength, callbackParameter);
        }
    }

    static abstract class TrieNode<T> {
        /**
         * Dummy value used for root node. Value can be anything as it's never referenced.
         */
        private static final char ROOT_NODE_CHARACTER_VALUE = 0;  // ASCII null character.

        /**
         * How much to expand the children array when resizing.
         */
        private static final int CHILDREN_ARRAY_INCREASE_SIZE_INCREMENT = 2;

        /**
         * Character this node represents.
         * This field is ignored for the root node (which does not represent any character).
         */
        private final char nodeValue;

        /**
         * A compressed graph path that represents the remaining pattern characters of a single child node.
         *
         * If present then child array is always null, although callbacks for other
         * end of patterns can also exist on this same node.
         */
        @Nullable
        private TrieCompressedPath<T> leaf;

        /**
         * All child nodes. Only present if no compressed leaf exist.
         *
         * Array is dynamically increased in size as needed,
         * and uses perfect hashing for the elements it contains.
         *
         * So if the array contains a given character,
         * the character will always map to the node with index: (character % arraySize).
         *
         * Elements not contained can collide with elements the array does contain,
         * so must compare the nodes character value.
         *
         * Alternatively this array could be a sorted and densely packed array,
         * and lookup is done using binary search.
         * That would save a small amount of memory because there's no null children entries,
         * but would give a worst case search of O(nlog(m)) where n is the number of
         * characters in the searched text and m is the maximum size of the sorted character arrays.
         * Using a hash table array always gives O(n) search time.
         * The memory usage here is very small (all Litho filters use ~10KB of memory),
         * so the more performant hash implementation is chosen.
         */
        @Nullable
        private TrieNode<T>[] children;

        /**
         * Callbacks for all patterns that end at this node.
         */
        @Nullable
        private List<TriePatternMatchedCallback<T>> endOfPatternCallback;

        TrieNode() {
            this.nodeValue = ROOT_NODE_CHARACTER_VALUE;
        }
        TrieNode(char nodeCharacterValue) {
            this.nodeValue = nodeCharacterValue;
        }

        /**
         * @param pattern       Pattern to add.
         * @param patternIndex  Current recursive index of the pattern.
         * @param patternLength Length of the pattern.
         * @param callback      Callback, where a value of NULL indicates to always accept a pattern match.
         */
        private void addPattern(T pattern, int patternIndex, int patternLength,
                                @Nullable TriePatternMatchedCallback<T> callback) {
            if (patternIndex == patternLength) { // Reached the end of the pattern.
                if (endOfPatternCallback == null) {
                    endOfPatternCallback = new ArrayList<>(1);
                }
                endOfPatternCallback.add(callback);
                return;
            }

            if (leaf != null) {
                // Reached end of the graph and a leaf exist.
                // Recursively call back into this method and push the existing leaf down 1 level.
                if (children != null) throw new IllegalStateException();
                //noinspection unchecked
                children = new TrieNode[1];
                TrieCompressedPath<T> temp = leaf;
                leaf = null;
                addPattern(temp.pattern, temp.patternStartIndex, temp.patternLength, temp.callback);
                // Continue onward and add the parameter pattern.
            } else if (children == null) {
                leaf = new TrieCompressedPath<>(pattern, patternIndex, patternLength, callback);
                return;
            }

            final char character = getCharValue(pattern, patternIndex);
            final int arrayIndex = hashIndexForTableSize(children.length, character);
            TrieNode<T> child = children[arrayIndex];
            if (child == null) {
                child = createNode(character);
                children[arrayIndex] = child;
            } else if (child.nodeValue != character) {
                // Hash collision. Resize the table until perfect hashing is found.
                child = createNode(character);
                expandChildArray(child);
            }
            child.addPattern(pattern, patternIndex + 1, patternLength, callback);
        }

        /**
         * Resizes the children table until all nodes hash to exactly one array index.
         */
        private void expandChildArray(TrieNode<T> child) {
            int replacementArraySize = Objects.requireNonNull(children).length;
            while (true) {
                replacementArraySize += CHILDREN_ARRAY_INCREASE_SIZE_INCREMENT;
                //noinspection unchecked
                TrieNode<T>[] replacement = new TrieNode[replacementArraySize];
                addNodeToArray(replacement, child);

                boolean collision = false;
                for (TrieNode<T> existingChild : children) {
                    if (existingChild != null) {
                        if (!addNodeToArray(replacement, existingChild)) {
                            collision = true;
                            break;
                        }
                    }
                }
                if (collision) {
                    continue;
                }

                children = replacement;
                return;
            }
        }

        private static <T> boolean addNodeToArray(TrieNode<T>[] array, TrieNode<T> childToAdd) {
            final int insertIndex = hashIndexForTableSize(array.length, childToAdd.nodeValue);
            if (array[insertIndex] != null ) {
                return false; // Collision.
            }
            array[insertIndex] = childToAdd;
            return true;
        }

        private static int hashIndexForTableSize(int arraySize, char nodeValue) {
            return nodeValue % arraySize;
        }

        /**
         * This method is static and uses a loop to avoid all recursion.
         * This is done for performance since the JVM does not optimize tail recursion.
         *
         * @param startNode          Node to start the search from.
         * @param searchText         Text to search for patterns in.
         * @param searchTextIndex    Start index, inclusive.
         * @param searchTextEndIndex End index, exclusive.
         * @return If any pattern matches, and it's associated callback halted the search.
         */
        private static <T> boolean matches(final TrieNode<T> startNode, final T searchText,
                                           int searchTextIndex, final int searchTextEndIndex,
                                           final Object callbackParameter) {
            TrieNode<T> node = startNode;
            int currentMatchLength = 0;

            while (true) {
                TrieCompressedPath<T> leaf = node.leaf;
                if (leaf != null && leaf.matches(startNode, searchText, searchTextEndIndex, searchTextIndex, callbackParameter)) {
                    return true; // Leaf exists and it matched the search text.
                }

                List<TriePatternMatchedCallback<T>> endOfPatternCallback = node.endOfPatternCallback;
                if (endOfPatternCallback != null) {
                    final int matchStartIndex = searchTextIndex - currentMatchLength;
                    for (@Nullable TriePatternMatchedCallback<T> callback : endOfPatternCallback) {
                        if (callback == null) {
                            return true; // No callback and all matches are valid.
                        }
                        if (callback.patternMatched(searchText, matchStartIndex, currentMatchLength, callbackParameter)) {
                            return true; // Callback confirmed the match.
                        }
                    }
                }

                TrieNode<T>[] children = node.children;
                if (children == null) {
                    return false; // Reached a graph end point and there's no further patterns to search.
                }
                if (searchTextIndex == searchTextEndIndex) {
                    return false; // Reached end of the search text and found no matches.
                }

                // Use the start node to reduce VM method lookup, since all nodes are the same class type.
                final char character = startNode.getCharValue(searchText, searchTextIndex);
                final int arrayIndex = hashIndexForTableSize(children.length, character);
                TrieNode<T> child = children[arrayIndex];
                if (child == null || child.nodeValue != character) {
                    return false;
                }

                node = child;
                searchTextIndex++;
                currentMatchLength++;
            }
        }

        /**
         * Gives an approximate memory usage.
         *
         * @return Estimated number of memory pointers used, starting from this node and including all children.
         */
        private int estimatedNumberOfPointersUsed() {
            int numberOfPointers = 4; // Number of fields in this class.
            if (leaf != null) {
                numberOfPointers += 4; // Number of fields in leaf node.
            }

            if (endOfPatternCallback != null) {
                numberOfPointers += endOfPatternCallback.size();
            }

            if (children != null) {
                numberOfPointers += children.length;
                for (TrieNode<T> child : children) {
                    if (child != null) {
                        numberOfPointers += child.estimatedNumberOfPointersUsed();
                    }
                }
            }
            return numberOfPointers;
        }

        abstract TrieNode<T> createNode(char nodeValue);
        abstract char getCharValue(T text, int index);
        abstract int getTextLength(T text);
    }

    /**
     * Root node, and it's children represent the first pattern characters.
     */
    private final TrieNode<T> root;

    /**
     * Patterns to match.
     */
    private final List<T> patterns = new ArrayList<>();

    @SafeVarargs
    TrieSearch(TrieNode<T> root, T... patterns) {
        this.root = Objects.requireNonNull(root);
        addPatterns(patterns);
    }

    @SafeVarargs
    public final void addPatterns(T... patterns) {
        for (T pattern : patterns) {
            addPattern(pattern);
        }
    }

    /**
     * Adds a pattern that will always return a positive match if found.
     *
     * @param pattern Pattern to add. Calling this with a zero length pattern does nothing.
     */
    public void addPattern(T pattern) {
        addPattern(pattern, root.getTextLength(pattern), null);
    }

    /**
     * @param pattern  Pattern to add. Calling this with a zero length pattern does nothing.
     * @param callback Callback to determine if searching should halt when a match is found.
     */
    public void addPattern(T pattern, TriePatternMatchedCallback<T> callback) {
        addPattern(pattern, root.getTextLength(pattern), Objects.requireNonNull(callback));
    }

    void addPattern(T pattern, int patternLength, @Nullable TriePatternMatchedCallback<T> callback) {
        if (patternLength == 0) return; // Nothing to match

        patterns.add(pattern);
        root.addPattern(pattern, 0, patternLength, callback);
    }

    public final boolean matches(T textToSearch) {
        return matches(textToSearch, 0);
    }

    public boolean matches(T textToSearch, Object callbackParameter) {
        return matches(textToSearch, 0, root.getTextLength(textToSearch),
                Objects.requireNonNull(callbackParameter));
    }

    public boolean matches(T textToSearch, int startIndex) {
        return matches(textToSearch, startIndex, root.getTextLength(textToSearch));
    }

    public final boolean matches(T textToSearch, int startIndex, int endIndex) {
        return matches(textToSearch, startIndex, endIndex, null);
    }

    /**
     * Searches through text, looking for any substring that matches any pattern in this tree.
     *
     * @param textToSearch Text to search through.
     * @param startIndex Index to start searching, inclusive value.
     * @param endIndex Index to stop matching, exclusive value.
     * @param callbackParameter Optional parameter passed to the callbacks.
     * @return If any pattern matched, and it's callback halted searching.
     */
    public boolean matches(T textToSearch, int startIndex, int endIndex, @Nullable Object callbackParameter) {
        return matches(textToSearch, root.getTextLength(textToSearch), startIndex, endIndex, callbackParameter);
    }

    private boolean matches(T textToSearch, int textToSearchLength, int startIndex, int endIndex,
                            @Nullable Object callbackParameter) {
        if (endIndex > textToSearchLength) {
            throw new IllegalArgumentException("endIndex: " + endIndex
                    + " is greater than texToSearchLength: " + textToSearchLength);
        }
        if (patterns.isEmpty()) {
            return false; // No patterns were added.
        }
        for (int i = startIndex; i < endIndex; i++) {
            if (TrieNode.matches(root, textToSearch, i, endIndex, callbackParameter)) return true;
        }
        return false;
    }

    /**
     * @return Estimated memory size (in kilobytes) of this instance.
     */
    public int getEstimatedMemorySize() {
        if (patterns.isEmpty()) {
            return 0;
        }
        // Assume the device has less than 32GB of ram (and can use pointer compression),
        // or the device is 32-bit.
        final int numberOfBytesPerPointer = 4;
        return (int) Math.ceil((numberOfBytesPerPointer * root.estimatedNumberOfPointersUsed()) / 1024.0);
    }

    public int numberOfPatterns() {
        return patterns.size();
    }

    public List<T> getPatterns() {
        return Collections.unmodifiableList(patterns);
    }
}
