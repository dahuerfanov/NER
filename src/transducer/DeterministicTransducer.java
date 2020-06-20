package transducer;

import java.io.Serializable;
import java.util.Stack;

public class DeterministicTransducer implements Serializable {

    public int d[][];
    public String w[][];
    public String rho[];

    public DeterministicTransducer(int[][] d, String[][] w, String[] rho) {
        this.d = d;
        this.w = w;
        this.rho = rho;
    }

    public int size() {
        return d.length;
    }

    public int[] applyTransducer(int tags[]) {
        int q = 0, j = 0;
        int tags2[] = new int[tags.length];
        for (int i = 0; i < tags.length; i++) {
            for (int k = 0; k < w[q][tags[i]].length(); k++) {
                tags2[j++] = w[q][tags[i]].charAt(k) - 'a';
            }
            q = d[q][tags[i]];
        }
        if (rho[q] != null) {
            for (int k = 0; k < rho[q].length(); k++) {
                tags2[j++] = rho[q].charAt(k) - 'a';
            }
        }
        if (j != tags2.length) {
            throw new RuntimeException("There is an error with the transducer.");
        }
        return tags2;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean vst[] = new boolean[d.length];
        Stack<Integer> S = new Stack<Integer>();
        S.push(0);
        vst[0] = true;
        int u;
        while (!S.isEmpty()) {
            u = S.pop();
            sb = sb.append(u).append(" (").append(rho[u] != null).append(", ").append(rho[u] != null ? rho[u] : "").append(") = ");
            for (int c = 0; c < Transducer.TRANSITIONS_NUM; c++) {
                if (d[u][c] < 0) {
                    continue;
                }
                sb = sb.append("(").append(d[u][c]).append(",").append((char) (c + 'a')).append(",").append(w[u][c]).append("), ");
            }
            sb = sb.append("\n");
            for (int c = 0; c < Transducer.TRANSITIONS_NUM; c++) {
                if (d[u][c] >= 0 && !vst[d[u][c]]) {
                    vst[d[u][c]] = true;
                    S.push(d[u][c]);
                }
            }
        }
        return sb.toString();
    }

}
