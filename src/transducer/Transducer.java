package transducer;

import utils.Pair;

import java.util.*;

public class Transducer {

    public static int TRANSITIONS_NUM = -1;
    public static int N = 20000;

    public List<List<Transition>> adj;
    public Set<Integer> F;

    public Transducer(List<List<Transition>> adj, Set<Integer> F) {
        this.adj = adj;
        this.F = F;
    }

    public int size() {
        return adj.size();
    }

    public static void addWildcardTransition(List<Transition> list, int q) {
        boolean ok[] = new boolean[TRANSITIONS_NUM];
        for (Transition t : list) {
            ok[t.i] = true;
        }
        for (int i = 0; i < TRANSITIONS_NUM; i++) {
            if (!ok[i]) {
                list.add(new Transition(i, (char) (i + 'a'), q));
            }
        }
    }

    public static Transducer localExtension(char p1[], char p2, final int trns, final int len) {
        List<List<Transition>> adj = new ArrayList<List<Transition>>();
        Set<Integer> F = new HashSet<Integer>();
        for (int q = -1, k; q < len - 1; q++) {
            adj.add(new LinkedList<Transition>());
            for (int a = 0; a < TRANSITIONS_NUM; a++) {
                k = Math.min(len, q + 2);
                do {
                    k--;
                } while (!isSuffix(p1, k, q, (char) (a + 'a')));
                adj.get(q + 1).add(new Transition(a, (char) (a + 'a'), k + 1));
            }
            F.add(q + 1);
        }
        int sink = adj.size(), node;
        adj.add(null);
        if (trns == len - 1) {
            adj.get(trns).add(new Transition(p1[trns] - 'a', p2, node = 0));
        } else {
            adj.get(trns).add(new Transition(p1[trns] - 'a', p2, node = adj.size()));
            for (int i = trns + 1; i < len - 1; i++) {
                adj.add(new LinkedList<Transition>());
                adj.get(adj.size() - 1).add(new Transition(p1[i] - 'a', p1[i], adj.size()));
                addWildcardTransition(adj.get(adj.size() - 1), sink);
            }
            adj.add(new LinkedList<Transition>());
            adj.get(adj.size() - 1).add(new Transition(p1[len - 1] - 'a', p1[len - 1], 0));
            addWildcardTransition(adj.get(adj.size() - 1), sink);
        }
        boolean equal;
        char s[] = new char[2 * len];
        int n;
        for (int i = trns + 1; i < len; i++) {
            equal = true;
            for (int j = trns - 1, k = i - 1; j >= 0 && equal; j--, k--) {
                if (p1[j] != p1[k]) {
                    equal = false;
                }
            }
            if (!equal) {
                continue;
            }
            n = 0;
            for (int j = trns; j < i; j++) {
                s[n++] = p1[j];
            }
            for (int j = trns; j < len; j++) {
                s[n++] = p1[j];
            }
            equal = true;
            for (int j = 0, k = i - trns; j < len - trns; j++, k++) {
                if (s[j] != s[k]) {
                    equal = false;
                    break;
                }
            }
            if (!equal) {
                adj.get(i).add(new Transition(p1[trns] - 'a', p2, node));
            }
        }
        return new Transducer(adj, F);
    }

    private static boolean isSuffix(char p[], int k, int q, char a) {
        if (k == -1) {
            return true;
        }
        if (p[k] != a) {
            return false;
        }
        for (int i = k - 1, j = q; i >= 0; i--, j--) {
            if (j < 0) {
                return false;
            }
            if (p[i] != p[j]) {
                return false;
            }
        }
        return true;
    }

    public static Transducer compose(Transducer t1, Transducer t2) { // t1 o t2
        int n = 0;
        int map[][] = new int[t1.size()][t2.size()];
        int[] Q_x = new int[t1.size() * t2.size()], Q_y = new int[t1.size() * t2.size()];
        ArrayList<List<Transition>> _adj = new ArrayList<List<Transition>>();
        Set<Integer> _F = new HashSet<Integer>();
        Q_x[n] = 0;
        Q_y[n] = 0; //initial state
        map[0][0] = ++n;
        _adj.add(null);
        int u_x, u_y, v_x, v_y, idx;
        for (int i = 0; i < n; i++) {
            u_x = Q_x[i];
            u_y = Q_y[i];
            idx = map[u_x][u_y] - 1;
            if (t1.F.contains(u_x) && t2.F.contains(u_y)) {
                _F.add(idx);
            }
            if (t1.adj.get(u_x) == null || t2.adj.get(u_y) == null) {
                continue;
            }
            for (Transition tr2 : t2.adj.get(u_y)) {
                for (Transition tr1 : t1.adj.get(u_x)) {
                    if (tr2.o - 'a' == tr1.i) {
                        v_x = tr1.q;
                        v_y = tr2.q;
                        if (map[v_x][v_y] == 0) {
                            _adj.add(null);
                            Q_x[n] = v_x;
                            Q_y[n] = v_y;
                            map[v_x][v_y] = ++n;
                        }
                        if (_adj.get(idx) == null) {
                            _adj.set(idx, new LinkedList<Transition>());
                        }
                        _adj.get(idx).add(new Transition(tr2.i, tr1.o, map[v_x][v_y] - 1));
                    }
                }
            }
        }
        return new Transducer(_adj, _F);
    }

    private int maxPrefix(String s1, String s2, char s2cont, int acc) {
        int i = 0;
        while (i < s1.length() && i <= s2.length() && i < acc) {
            if (i < s2.length()) {
                if (s1.charAt(i) != s2.charAt(i)) {
                    break;
                }
            } else {
                if (s1.charAt(i) != s2cont) {
                    break;
                }
            }
            i++;
        }
        return i;
    }

    private String substring(String s, char scont, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < s.length(); i++) {
            sb.append(s.charAt(i));
        }
        if (from <= s.length()) {
            sb.append(scont);
        }
        return sb.toString();
    }

    public DeterministicTransducer determinizeTransducer() {
        int d[][] = new int[N][TRANSITIONS_NUM];
        String w_det[][] = new String[N][TRANSITIONS_NUM];
        String rho[] = new String[N];
        List<Set<Pair<Integer, String>>> sets = new ArrayList<Set<Pair<Integer, String>>>();
        Set<Pair<Integer, String>> set = new HashSet<Pair<Integer, String>>();
        Set<Pair<Integer, String>> set_[] = new Set[TRANSITIONS_NUM];
        set.add(new Pair(0, ""));
        sets.add(set);
        int n = 1, e;
        String w[] = new String[TRANSITIONS_NUM];
        int w_length[] = new int[TRANSITIONS_NUM];
        for (int q = 0; q < n; q++) {
            set = sets.get(q);
            Arrays.fill(d[q], -1);
            Arrays.fill(w, null);
            Arrays.fill(set_, null);
            for (Pair<Integer, String> p : set) {
                if (F.contains(p.first)) {
                    if (rho[q] != null && !p.second.equals(rho[q])) {
                        System.out.println(p.second + " != " + rho[q]);
                        throw new RuntimeException("Function rho is not well defined, there is no deterministic equivalent transducer.");
                    } else if (rho[q] == null) {
                        rho[q] = p.second;
                    }
                }
                if (adj.get(p.first) == null) {
                    continue;
                }
                for (Transition t : adj.get(p.first)) {
                    if (w[t.i] == null) {
                        w[t.i] = p.second + t.o;
                        w_length[t.i] = w[t.i].length();
                    } else {
                        w_length[t.i] = maxPrefix(w[t.i], p.second, t.o, w_length[t.i]);
                    }
                }
            }
            for (Pair<Integer, String> pair : set) {
                if (adj.get(pair.first) == null) {
                    continue;
                }
                for (Transition t : adj.get(pair.first)) {
                    if (w[t.i] == null) {
                        continue;
                    }
                    if (set_[t.i] == null) {
                        set_[t.i] = new HashSet<Pair<Integer, String>>();
                    }
                    set_[t.i].add(new Pair(t.q, substring(pair.second, t.o, w_length[t.i])));
                }
            }
            for (int j = 0; j < TRANSITIONS_NUM; j++) {
                if (w[j] == null) {
                    continue;
                }
                e = -1;
                for (int i = 0; e < 0 && i < n; i++) {
                    if (sets.get(i).equals(set_[j])) {
                        e = i;
                    }
                }
                if (e < 0) {
                    e = sets.size();
                    sets.add(set_[j]);
                    n++;
                }
                d[q][j] = e;
                w_det[q][j] = w[j].substring(0, w_length[j]);
            }
        }
        int d2[][] = new int[n][];
        String w_det2[][] = new String[n][];
        String rho2[] = new String[n];
        for (int i = 0; i < n; i++) {
            d2[i] = d[i];
            w_det2[i] = w_det[i];
            rho2[i] = rho[i];
        }
        return new DeterministicTransducer(d2, w_det2, rho2);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean vst[] = new boolean[adj.size()];
        Stack<Integer> S = new Stack<Integer>();
        S.push(0);
        vst[0] = true;
        List<Transition> list;
        int u;
        while (!S.isEmpty()) {
            u = S.pop();
            list = adj.get(u);
            sb = sb.append(u).append(": (").append((F != null && F.contains(u)) ? "true" : "false").append(") = ");
            if (list == null) {
                sb.append("\n");
                continue;
            }
            for (Transition t : list) {
                sb = sb.append("(").append(t.q).append(",").append((char) (t.i + 'a')).append(",").append(t.o).append("), ");
            }
            sb = sb.append("\n");
            for (Transition t : list) {
                if (!vst[t.q]) {
                    vst[t.q] = true;
                    S.push(t.q);
                }
            }
        }
        return sb.toString();
    }
}
