package contextualTagger;

import java.io.Serializable;
import lexicalTagger.Trie;
import transducer.DeterministicTransducer;

public class Model implements Serializable {

    public Trie trie;
    public Trie trieSuffixes;
    public Trie trieShapes;
    public DeterministicTransducer dt;

    public Model(Trie trie, Trie trieSuffixes, Trie trieShapes, DeterministicTransducer dt) {
        this.trie = trie;
        this.trieSuffixes = trieSuffixes;
        this.trieShapes = trieShapes;
        this.dt = dt;
    }
}
