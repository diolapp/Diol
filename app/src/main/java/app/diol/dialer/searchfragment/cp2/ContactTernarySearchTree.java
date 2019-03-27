/*
 *  Copyright (C) 2019  The Diol App Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.diol.dialer.searchfragment.cp2;

import android.support.v4.util.ArraySet;
import android.text.TextUtils;

import java.util.Set;

/**
 * Ternary Search Tree for searching a list of contacts.
 */
public class ContactTernarySearchTree {

    private Node root;

    /**
     * Add {@code value} to all middle and end {@link Node#values} that correspond to {@code key}.
     *
     * <p>For example, if {@code key} were "FOO", {@code value} would be added to nodes "F", "O" and
     * "O". But if the traversal required visiting {@link Node#left} or {@link Node#right}, {@code
     * value} wouldn't be added to those nodes.
     */
    public void put(String key, int value) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        root = put(root, key, value, 0);
    }

    private Node put(Node node, String key, int value, int position) {
        char c = key.charAt(position);
        if (node == null) {
            node = new Node();
            node.key = c;
        }
        if (c < node.key) {
            node.left = put(node.left, key, value, position);
        } else if (c > node.key) {
            node.right = put(node.right, key, value, position);
        } else if (position < key.length() - 1) {
            node.values.add(value);
            node.mid = put(node.mid, key, value, position + 1);
        } else {
            node.values.add(value);
        }
        return node;
    }

    /**
     * Returns true if {@code key} is contained in the trie.
     */
    public boolean contains(String key) {
        return !get(key).isEmpty();
    }

    /**
     * Return value stored at Node (in this case, a set of integers).
     */
    public Set<Integer> get(String key) {
        Node x = get(root, key, 0);
        return x == null ? new ArraySet<>() : x.values;
    }

    private Node get(Node node, String key, int position) {
        if (node == null) {
            return null;
        }
        char c = key.charAt(position);
        if (c < node.key) {
            return get(node.left, key, position);
        } else if (c > node.key) {
            return get(node.right, key, position);
        } else if (position < key.length() - 1) {
            return get(node.mid, key, position + 1);
        } else {
            return node;
        }
    }

    /**
     * Node in ternary search trie. Children are denoted as left, middle and right nodes.
     */
    private static class Node {
        private final Set<Integer> values = new ArraySet<>();
        private char key;
        private Node left;
        private Node mid;
        private Node right;
    }
}
