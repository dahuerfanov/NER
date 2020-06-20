package lexicalTagger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class CompressedTrie implements Serializable {

    private HashMap<Node, Node> R;
    private final Node q_0;
    private Stack<Node> P;
    private Node s, s_1, clone, r, first;
    private int i, u;

    public CompressedTrie() {
        R = new HashMap<Node, Node>();
        q_0 = new Node();
        q_0.inR = true;
        R.put(q_0, q_0);
        P = new Stack<Node>();
    }

    public void addWord(char w[], int length) {
        //w[length++] = (char)('0'+tagsEncoder.getCode(tag));
        s = q_0;
        i = 0;
        P.push(s);
        while (i < length && (s_1 = s.edges.get(w[i])) != null && s_1.inDegree <= 1) {
            s = s_1;
            P.push(s);
            i++;
        }
        u = i;
        first = s;
        R.remove(s);
        s.inR = false;
        while (i < length && (s_1 = s.edges.get(w[i])) != null) {
            if (s.inR) {
                R.remove(s);
            }
            s.edges.put(w[i], clone = s_1.clone());
            if (s.inR) {
                R.put(s, s);
            }
            clone.inDegree = 1;
            s_1.inDegree--;
            s = clone;
            P.push(s);
            i++;
        }
        while (i < length) {
            s_1 = new Node();
            if (s.inR) {
                R.remove(s);
            }
            s.edges.put(w[i], s_1);
            if (s.inR) {
                R.put(s, s);
            }
            s_1.inDegree = 1;
            s = s_1;
            P.push(s);
            i++;
        }
        s.isFinal = true;
        //updateNodeCnt(s, tag);
        P.pop();
        while (!P.isEmpty()) {
            i--;
            if (R.containsKey(s)) {
                r = R.get(s);
                if (i == u && i > 0) {
                    first = P.peek();
                    P.peek().inR = false;
                    R.remove(P.peek());
                    u--;
                }
                if (P.peek().inR) {
                    R.remove(P.peek());
                }
                if (P.peek().edges.get(w[i]) != null) {
                    P.peek().edges.get(w[i]).inDegree--;
                }
                P.peek().edges.put(w[i], r);
                r.inDegree++;
                if (P.peek().inR) {
                    R.put(P.peek(), P.peek());
                }
            } else {
                s.inR = true;
                R.put(s, s);
                if (i == u) {
                    break;
                }
            }
            s = P.pop();
        }
        if (!R.containsKey(first)) {
            first.inR = true;
            R.put(first, first);
        }
        P.clear();
    }

    public void print() {
        dfs(new StringBuilder(), q_0);
        System.out.println("size = " + R.size());
    }

    private void dfs(StringBuilder acc, Node state) {
        if (state.isFinal) {
            System.out.println(acc);
        }
        for (char c : state.edges.keySet()) {
            dfs(acc.append(c), state.edges.get(c));
            acc.replace(acc.length() - 1, acc.length(), "");
        }
    }

    public boolean containsWord(String w) {
        return containsWord(w.toCharArray(), q_0, 0);
    }

    private boolean containsWord(char w[], Node state, int i) {
        if (i == w.length) {
            return state.isFinal;
        }
        if (state.edges.containsKey(w[i])) {
            return containsWord(w, state.edges.get(w[i]), i + 1);
        } else {
            return false;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File("Countries.txt")));
        String line, toks[];
        CompressedTrie cTrie = new CompressedTrie();
        while ((line = reader.readLine()) != null) {
            toks = line.split("[\t]+");
            cTrie.addWord(toks[15].toCharArray(), 0);

        }
    }

    private static class Node implements Serializable {

        Map<Character, Node> edges;
        boolean isFinal, inR;
        int inDegree;

        Node() {
            isFinal = inR = false;
            edges = new HashMap<Character, Node>();
            inDegree = 0;
        }

        @Override
        public Node clone() {
            Node clone = new Node();
            clone.isFinal = isFinal;
            clone.inDegree = 0;
            clone.inR = false;
            for (char c : edges.keySet()) {
                clone.edges.put(c, edges.get(c));
            }
            return clone;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Node)) {
                return false;
            }

            Node node = (Node) o;

            if (isFinal != node.isFinal) {
                return false;
            }
            if (edges.keySet().size() != node.edges.keySet().size()) {
                return false;
            }
            for (char c : edges.keySet()) {
                if (!node.edges.containsKey(c) || edges.get(c) != node.edges.get(c)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = 0;
            for (char c : edges.keySet()) {
                result = result + c;
            }
            result = 31 * result + (isFinal ? 1 : 0);
            return result;
        }
    }
}
