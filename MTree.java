package internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * <p>
 *     My implementation of a Tree collection in Java. This class acts as both an
 *     individual node and a tree data structure at the same time.
 * </p>
 * <p>
 *     Below is an example implementation.
 * </p>
 * <pre>
 *     MTree&lt;String&gt; programmingLanguages = new MTree&lt;&gt;("Languages");
 *     programmingLanguages.add("Compiled Languages", "Interpreted Languages","Esoteric Languages");
 *
 *     programmingLanguages.get(0).add("Java", "C++", "Go");
 *     programmingLanguages.get(1).add("Python", "JavaScript");
 *     programmingLanguages.get(2).add("BrainF***");
 *
 *     programmingLanguages.get(0).get(0).add("Java is cool", "Android :)");
 *     programmingLanguages.get(0).get(1).add("C++ is used to make games.");
 *
 *     programmingLanguages.get(2).get(0).add("Only uses eight pointers");
 *
 *     programmingLanguages.print();
 * </pre>
 * <p>
 *     This code would output the following:
 * </p>
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
 *  	 	    └── Only uses eight pointers
 * </pre>
 *
 * This class was designed specifically in mind to allow for the splitting
 * of equations, following the PEMDAS rule, while building a calculator.
 *
 * @param <T> The type of the data type stored in the tree.
 */
public class MTree<T> {
    /**
     * The actual value stored in this node of the tree.
     */
    private T DATA;

    /**
     * The parent node to this instance. If this is the root node, its parent is {@code null}
     */
    private MTree<T> PARENT;

    /**
     * An {@link ArrayList} containing all nodes that are children to this node.
     */
    private final List<MTree<T>> CHILDREN = new ArrayList<>();

    /**
     * Used when printing the grid; ignore.
     */
    private boolean completed = false;

    /**
     * These are the characters used to visualize the tree.
     */
    private static final char[] SPECIAL_CHARACTERS = {0x2502, 0x2514, 0x251C, 0x2500};

    /**
     * Construct a new {@link MTree} with no starting node.
     */
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

    /**
     * Get the Nth child to this node.
     *
     * @param index an {@code int} index
     * @return the {@link MTree} node at the specified index.
     * @throws IndexOutOfBoundsException if the index supplied is greater than the total amount
     *                                   of child nodes connected to this node, or less than zero.
     */
    public MTree<T> get(int index) throws IndexOutOfBoundsException {
        return CHILDREN.get(index);
    }

    /**
     * Add pre-determined nodes to this node (varargs).
     *
     * @param nodes the nodes to be added.
     */
    @SafeVarargs
    public final void add(MTree<T>... nodes) {
        for (MTree<T> node : nodes) {
            node.setParent(this);
            this.addChild(node);
        }
    }

    /**
     * Create nodes to be added to this node from the raw input type (varargs).
     *
     * @param nodes the raw data to be added to this node.
     */
    @SafeVarargs
    public final void add(T... nodes) {
        @SuppressWarnings("unchecked")
        MTree<T>[] nodes1 = new MTree[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            nodes1[i] = new MTree<>(nodes[i]);
        }

        add(nodes1);
    }

    /**
     * Set a node as this tree's child.
     *
     * @param child the node
     */
    private void addChild(MTree<T> child) {
        this.CHILDREN.add(child);
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
     * Simple recursive tree traversal algorithm.
     *
     * @param res    Recursive field: to use, initialize a new {@link StringBuilder}
     * @param init   This is the grid to be traversed.
     * @param offset Recursive field: to use, set to {@code 0}.
     * @return a {@link String} containing a formatted tree, one that resembles the output from
     * the {@code tree} command in the windows command line.
     */
    private String buildFormattedTree(StringBuilder res, MTree<T> init, int offset) {
        // Function that returns a String repeating a character (c) a certain amount of times (reps).
        final BiFunction<Integer, Character, String> repeatCharacters = (reps, c) -> {
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < reps; i++) {
                str.append(c);
            }
            return str.toString();
        };

        List<MTree<T>> children = init.getChildren();
        if (children.size() == 0) {
            return "";
        } else {
            for (int i = 0; i < children.size(); i++) {
                MTree<T> node = children.get(i);
                MTree<T> temp = init;
                StringBuilder str = new StringBuilder();

                for (int j = 0; j < offset; j++) {
                    temp = temp.getParent();
                    str.append(repeatCharacters.apply(1, '\t'));
                    str.append(temp.completed ? ' ' : SPECIAL_CHARACTERS[0]);
                }

                res.append(str.reverse());
                res.append(i == children.size() - 1
                        ? SPECIAL_CHARACTERS[1]
                        : SPECIAL_CHARACTERS[2]).append(repeatCharacters.apply(2, SPECIAL_CHARACTERS[3]));

                if (i == children.size() - 1) {
                    node.getParent().completed = true;
                }

                res.append(' ').append(ln(node.getNodeValue()));
                buildFormattedTree(res, node, offset + 1);

                if (i == children.size() - 1) {
                    init.completed = true;
                }
            }
            return res.substring(0, res.length() - 1);
        }
    }

    /**
     * Print this tree's content in a natural, easy to follow manner.
     *
     * @see #buildFormattedTree(StringBuilder, MTree, int)
     */
    public void print() {
        String str = this.DATA == null ? this.getClass().getSimpleName() + '@' + this.hashCode() : this.DATA.toString();
        str += this.getChildren().size() == 0 ? "" : pln(buildFormattedTree(new StringBuilder(), this, 0));
        System.out.println(str);
    }

    public static <K> String pln(K s) {
        return '\n' + s.toString();
    }

    public static <K> String ln(K s) {
        return s.toString() + '\n';
    }

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
        return "MTree{" +
                "DATA=" + DATA +
                ", CHILDREN=" + CHILDREN +
                '}';
    }
}
