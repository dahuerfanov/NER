package contextualTagger;

import java.io.Serializable;

public class Rule implements Serializable {

    public char pat[];
    public char p;
    public int trns;

    public Rule(int trns, char p, char... a) {
        this.trns = trns;
        this.p = p;
        this.pat = new char[a.length];
        System.arraycopy(a, 0, pat, 0, a.length);
    }
}
