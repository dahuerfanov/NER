package lexicalTagger;

import java.io.Serializable;
import java.util.HashMap;

public class Trie implements Serializable {

    private final Node root;
    private int maxLength;
    private int numberOfTags;

    public Trie(int numberOfTags) {
        this.numberOfTags = numberOfTags;
        root = new Node(numberOfTags);
    }

    public void addWord(String w, int tag) {
        Node node = root;
        maxLength = maxLength < w.length() ? w.length() : maxLength;
        for (int i = 0; i < w.length(); i++) {
            if (!node.children.containsKey(w.charAt(i))) {
                node.children.put(w.charAt(i), new Node(numberOfTags));
            }
            node = node.children.get(w.charAt(i));
        }
        node.isFinal = true;
        node.tagCnt[tag]++;
        if (node.tagCnt[tag] > node.tagCnt[node.maxIndex]) {
            node.maxIndex = tag;
        }
    }

    public int getTag(String w) {
        Node node = root;
        for (int i = 0; i < w.length(); i++) {
            if (node.children.containsKey(w.charAt(i))) {
                node = node.children.get(w.charAt(i));
            } else {
                return -1;
            }
        }
        if (!node.isFinal) {
            return -1;
        }
        return node.maxIndex;
    }

    public boolean hasTag(String w, int tag) {
        Node node = root;
        for (int i = 0; i < w.length(); i++) {
            if (node.children.containsKey(w.charAt(i))) {
                node = node.children.get(w.charAt(i));
            } else {
                return true;
            }
        }
        if (!node.isFinal) {
            return true;
        }
        return node.tagCnt[tag] > 0;
    }

    public void print() {
        StringBuilder sb = new StringBuilder();
        dfsPrint(root, sb);
    }

    private void dfsPrint(Node node, StringBuilder sb) {
        if (node.isFinal) {
            System.out.println(sb);
        }
        for (char c : node.children.keySet()) {
            sb.append(c);
            dfsPrint(node.children.get(c), sb);
            sb.delete(sb.length() - 1, sb.length());
        }
    }

    static private class Node implements Serializable {

        boolean isFinal;
        int tagCnt[], maxIndex = 0;
        HashMap<Character, Node> children;

        Node(int n) {
            tagCnt = new int[n];
            children = new HashMap<Character, Node>();
        }
    }
}
