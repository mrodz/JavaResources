import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>An implementation of a Tree Data Structure in Java. This class acts as both
 * an individual node and a tree data structure at the same time. It can serve
 * useful when establishing relational and hierarchical relationships between
 * objects, and differs from a standard Binary Tree.</p>
 *
 * <p>Below is a sample implementation.</p>
 * <p><pre>
 *     MTree&lt;String&gt; exampleTree = new MTree&lt;&gt;("Languages");
 *     exampleTree.insert("Compiled", "Interpreted", "Esoteric");
 *
 *     exampleTree.getNode(0).insert("Java", "C++", "Go");
 *     exampleTree.getNode(1).insert("Python", "JavaScript");
 *     exampleTree.getNode(2).insert("BrainF***");
 *
 *     exampleTree.getNode(0).getNode(0).insert("Java is cool", "Android :)");
 *     exampleTree.getNode(0).getNode(1).insert("C++ is used to make games.");
 *
 *     exampleTree.deepSearchChildren("BrainF***").get()
 *             .insert("Only uses eight pointers");
 *
 *     programmingLanguages.print();
 * </pre></p>
 * <p>This code would output the following:</p>
 * <pre>
 *     Languages
 *     ├── Compiled Languages
 *     │    ├── Java
 *     │    │    ├── Java is cool
 *     │    │    └── Android :)
 *     │    ├── C++
 *     │    │    └── C++ is used to make games.
 *     │    └── Go
 *     ├── Interpreted Languages
 *     │    ├── Python
 *     │    └── JavaScript
 *     └── Esoteric Languages
 *          └── BrainF***
 *  	         └── Only uses eight pointers
 * </pre>
 *
 * <p>Most of the calculations and method calls operate with a general <tt>O(n)</tt>
 * computational complexity. This is because every node is only visited once,
 * across every method provided in this class.</p>
 *
 * <p>Note: one should take care when providing a mutable object as the key value
 * initializing a node, for the behavior of the tree is not specified should
 * a node's {@link #equals(Object)} method be set to return a different value
 * in a separate thread.</p>
 *
 * @param <T> The type of the data stored in the tree.
 * @author github@mrodz
 * @since 8
 */
public class MTree<T> {
    /**
     * The actual value stored in this node of the tree.
     */
    private volatile T DATA;

    /**
     * The parent node to this instance. If this is the root node, its parent is {@code null}
     */
    private volatile MTree<T> PARENT;

    /**
     * An {@link ArrayList} containing all nodes that are children to this node.
     */
    private final List<MTree<T>> CHILDREN = new ArrayList<>();

    /**
     * Used when printing the grid; ignore.
     */
    private volatile boolean completed = false;

    /**
     * Stores all of the values associated with the children to this node.
     */
    private final Set<T> entries = new HashSet<>();

    /**
     * These are the characters used to visualize the tree.
     */
    private static final char[] SPECIAL_CHARACTERS = {0x2502, 0x2514, 0x251C, 0x2500};

    //
    // Preferences
    //

    /**
     * Specify whether or any special characters should be escaped when
     * getting a fancy {@link String} version of the table (preferred: {@code true}).
     * @see #cancelEscapeSequences
     */
    private boolean escapeCharacters = true;

    /**
     * Whether or not to use Java's native tab (\t) for horizontal spacing.
     */
    private static final boolean USE_NATIVE_TAB = false;

    /**
     * If {@link #USE_NATIVE_TAB} is {@code false}, how many spaces to use instead.
     */
    private static final int REPLACEMENT_TAB_SPACES = 3;

    //
    // CONSTRUCTORS
    //

    /**
     * Construct a new {@link MTree} with no starting node.
     *
     * @deprecated - a tree should contain a root element, for best clarity.
     */
    @Deprecated
    public MTree() {
    }

    /**
     * Construct a new {@link MTree} with a specific starting node.
     *
     * @param startingNode the node to serve as this tree's root.
     */
    public MTree(T startingNode) {
        if (startingNode == null) throw new NullPointerException("Root Node cannot be null");
        this.DATA = startingNode;
    }

    //
    // SPECIALIZED METHODS
    //

    /**
     * Add pre-determined nodes to this node (varargs).
     *
     * @param nodes the nodes to be added.
     */
    @SafeVarargs
    public final void insert(MTree<T>... nodes) {
        if (Arrays.stream(nodes).anyMatch(Objects::isNull)) {
            throw new NullPointerException("null input");
        }
        for (MTree<T> node : nodes) {
            if (this.entries.contains(node.DATA)) {
                throw new IllegalArgumentException("Duplicate entry into tree: " + node);
            } else {
                node.setParent(this);
                this.addChild(node);
                this.entries.add(node.DATA);
            }
        }
    }

    /**
     * Create nodes to be added to this node from the raw input type (varargs).
     *
     * @param nodes the raw data to be added to this node.
     */
    @SafeVarargs
    public final void insert(T... nodes) {
        if (Arrays.stream(nodes).anyMatch(Objects::isNull)) {
            throw new NullPointerException("null input");
        }

        @SuppressWarnings("unchecked")
        MTree<T>[] nodes1 = new MTree[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            nodes1[i] = new MTree<>(nodes[i]);
        }

        insert(nodes1);
    }

    /**
     * Exclusively search the children that only span immediately from this node
     * (ie. depth of one) for a node with a matching value.
     *
     * @param data The object to be matched
     * @return An {@link Optional} containing the node with the matching {@link #DATA}
     * to the object supplied, if found; otherwise, {@link Optional#empty()}
     */
    public final Optional<MTree<T>> searchChildrenFor(final T data) {
        if (!this.entries.contains(data)) {
            return Optional.empty();
        }

        return Optional.ofNullable(this.CHILDREN.stream()
                .filter(n -> n.DATA.equals(data))
                .collect(Collectors.toList()).get(0));
    }

    /**
     * Search <u>every</u> child connected to this node for a node with a matching value.
     * This method will return the shallowest match exclusively, to avoid mix-ups.
     *
     * @param data The object to be matched
     * @return An {@link Optional} containing the node with the matching {@link #DATA}
     * to the object supplied, if found; otherwise, {@link Optional#empty()}
     * @see MTree#find(Object, MTree)
     */
    public final Optional<MTree<T>> deepSearchChildrenFor(final T data) {
        return MTree.find(data, this);
    }

    /**
     * Recursive function to search for an object in all of the nodes (children)
     * spanning from a specified target node. This method will return the shallowest match
     * exclusively, to avoid mix-ups.
     *
     * @param object The object
     * @param node   The parent node
     * @param <R>    The type of the returning {@link Optional}, the object being
     *               searched for, and the type of the {@link #DATA} being stored
     *               in the parent node.
     * @return An {@link Optional} containing the node with the matching {@link #DATA}
     * to the object supplied, if found; otherwise, {@link Optional#empty()}
     * @see #searchChildrenFor(Object)
     */
    public static <R> Optional<MTree<R>> find(final R object, MTree<R> node) {
        if (node.DATA.equals(object)) {
            return Optional.of(node);
        } else if (node.entries.contains(object)) {
            Stream<MTree<R>> stream = node.CHILDREN.stream().filter(n -> n.DATA.equals(object));
            List<MTree<R>> collected = stream.collect(Collectors.toList());
            if (collected.size() != 1) {
                throw new IllegalStateException("Multiple children to " + node + "have the same canonical value.");
            }
            return Optional.of(collected.get(0));
        }

        for (MTree<R> child : node.CHILDREN) {
            Optional<MTree<R>> obj = find(object, child);
            if (obj.isPresent()) {
                return obj;
            }
        }

        // Cannot find value
        return Optional.empty();
    }

    /**
     * Simple recursive tree traversal algorithm.
     *
     * @param res    Recursive field: to use, initialize a new {@link StringBuilder}
     * @param init   This is the grid to be traversed.
     * @param offset Recursive field, represents the amount of tab/spaces: to use, set to {@code 0}.
     * @return a {@link String} containing a formatted tree, one that resembles the output from
     * the {@code tree} command in the windows command line.
     */
    private String buildFormattedTree(StringBuilder res, MTree<T> init, int offset) {
        List<MTree<T>> children = init.getChildren();

        // No children exist; the node is the last node in this lineage
        if (children.size() == 0) {
            return "";
        }

        // Go through each child
        for (int i = 0; i < children.size(); i++) {
            MTree<T> node = children.get(i);
            MTree<T> temp = init;
            StringBuilder str = new StringBuilder();

            // Apply the cosmetic tabs and pipes
            for (int j = 0; j < offset; j++) {
                temp = temp.getParent();
                str.append(MTree.USE_NATIVE_TAB ? '\t' : repeatCharacters.apply(MTree.REPLACEMENT_TAB_SPACES, ' '));
                str.append(temp.completed ? ' ' : SPECIAL_CHARACTERS[0]);
            }

            boolean isLastElement = i == children.size() - 1;

            res.append(str.reverse());
            res.append(isLastElement
                    ? SPECIAL_CHARACTERS[1]
                    : SPECIAL_CHARACTERS[2]).append(repeatCharacters.apply(2, SPECIAL_CHARACTERS[3]));

            // is the node the last node to appear in the sequence (visually)
            if (isLastElement) {
                node.getParent().completed = true;
            }

            res.append(' ').append(ln(this.escapeCharacters
                    ? cancelEscapeSequences.apply(node.getNodeValue().toString())
                    : node.getNodeValue()));

            buildFormattedTree(res, node, offset + 1);
        }

        // clear excess newline, return value
        return res.substring(0, res.length() - 1);
    }

    /**
     * Print this tree's content in a natural, easy to follow manner.
     *
     * @see #buildFormattedTree(StringBuilder, MTree, int)
     */
    public void print() {
        System.out.println(this.getFancyString());
    }

    /**
     * Get this tree's content in a fancy format. Keep in mind that the
     * style of the return value depends on the viewport, since smaller
     * STDI/O's might not be able to a show the entirety of a long value 
     * on a single line.
     * 
     * @return a large formatted {@link String}
     * @see #print()
     */
    public String getFancyString() {
        // Handle deprecated functionality: data = null
        String str = this.DATA == null ? this.getClass().getSimpleName() + '@' + this.hashCode() : this.DATA.toString();
        str += this.getChildren().size() == 0 ? "" : pln(buildFormattedTree(new StringBuilder(), this, 0));
        return str;
    }

    /**
     * Set a node as this tree's child.
     *
     * @param child the node
     */
    private void addChild(MTree<T> child) {
        this.CHILDREN.add(child);
    }

    //
    // GETTERS + SETTERS
    //

    /**
     * Get the Nth child to this node.
     *
     * @param index an {@code int} index
     * @return the {@link MTree} node at the specified index.
     * @throws IndexOutOfBoundsException if the index supplied is greater than the total amount
     *                                   of child nodes connected to this node, or less than zero.
     */
    public MTree<T> getNode(int index) throws IndexOutOfBoundsException {
        return CHILDREN.get(index);
    }

    /**
     * Get the actual value stored in this node.
     *
     * @return the data
     */
    public T getNodeValue() {
        return DATA;
    }

    /**
     * Update the actual value stored in this node.
     *
     * @param data the data
     */
    public void setNodeValue(T data) {
        this.DATA = data;
    }

    /**
     * Get this node's parent.
     *
     * @return the parent.
     */
    public MTree<T> getParent() {
        return PARENT;
    }

    /**
     * Set this node's parent.
     *
     * @param parent the parent.
     */
    private void setParent(MTree<T> parent) {
        this.PARENT = parent;
    }

    /**
     * Get the children to this node as a {@link List}.
     *
     * @return the children
     */
    public List<MTree<T>> getChildren() {
        return CHILDREN;
    }

    /**
     * Get the canonical values associated with all of the children to this node,
     * of type {@code T}
     *
     * @return {@link #entries}
     */
    public Set<T> getEntries() {
        return entries;
    }

    /**
     * Get whether this instance of {@link MTree} is escaping character
     * sequences in pretty Strings.
     *
     * @return whether it is or is not
     */
    public boolean isEscapingCharacters() {
        return escapeCharacters;
    }

    /**
     * Set whether this instance of {@link MTree} is escaping character
     * sequences in pretty Strings.
     *
     * @param escapeCharacters the value
     */
    public void setEscapingCharacters(boolean escapeCharacters) {
        this.escapeCharacters = escapeCharacters;
    }

    //
    // RESOURCES
    //

    /**
     * Function that returns a {@link String} repeating a character (c) a certain amount of times (reps).
     */
    private static final BiFunction<Integer, Character, String> repeatCharacters = (reps, c) -> {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < reps; i++) {
            str.append(c);
        }
        return str.toString();
    };

    /**
     * Low-level function that returns a {@link String} canceling most ['\n', '\t', '\r']
     * of Java's escape characters.
     */
    public static final UnaryOperator<String> cancelEscapeSequences = (str) -> {
        StringBuilder res = new StringBuilder();
        char[] chars = str.toCharArray();
        for (char c : chars) {
            switch (c) {
                case 9:
                    res.append("\\t");
                    break;
                case 10:
                    res.append("\\n");
                    break;
                case 13:
                    res.append("\\r");
                    break;
                default:
                    res.append(c);
                    break;
            }
        }
        return res.toString();
    };

    public static <K> String pln(K s) {
        return '\n' + s.toString();
    }

    public static <K> String ln(K s) {
        return s.toString() + '\n';
    }

    //
    // OVERRIDES
    //

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MTree<?> that = (MTree<?>) o;
        return Objects.equals(DATA, that.DATA) && Objects.equals(PARENT, that.PARENT) && Objects.equals(CHILDREN, that.CHILDREN);
    }

    /**
     * Get this object's hash code.
     *
     * @return Get this object's hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(DATA, CHILDREN);
    }

    /**
     * Provides a compacted representation of this table. For a prettier visualization,
     * see {@link #print()}
     *
     * @return a {@link String} object containing this node's value and its children.
     */
    @Override
    public String toString() {
        return String.format("%s{DATA=%s, CHILDREN=%s}", this.getClass().getSimpleName(), DATA, CHILDREN);
    }
}
