package contextualTagger;

import IO.BIOReader;
import IO.TrainingReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import lexicalTagger.TagsEncoder;
import lexicalTagger.Trie;
import transducer.DeterministicTransducer;
import transducer.Transducer;
import utils.Pair;
import utils.Utils;

/**
 *
 * @author Diego
 */
public class LearningManager {

    public final static TagsEncoder tagsEncoder = new TagsEncoder(
            new String[]{"B-PER", "B-LOC", "B-ORG", "B-MISC", "I-PER", "I-LOC", "I-ORG", "I-MISC", "O", "EN", "DE", "POR", "SEGUN", "PUNCTUATION"});

    public static Model learn(File trainBig,
            File trainSmall, final int maxRules) throws Exception {
        int errMat[][] = new int[tagsEncoder.numberOfTags - 1][tagsEncoder.numberOfTags - 1];
        Transducer.TRANSITIONS_NUM = tagsEncoder.numberOfTags;
        Trie trie = new Trie(tagsEncoder.numberOfTags);
        Trie trieSuffixes = new Trie(tagsEncoder.numberOfTags);
        Trie trieShapes = new Trie(tagsEncoder.numberOfTags);
        Pair<String, Pair<String, String>> pair;
        TrainingReader bioReader = new BIOReader(trainBig);
        while ((pair = bioReader.getEntry()) != null) {
            if (pair.first.equals("")) {
                continue;
            }
            trie.addWord(pair.first.toLowerCase(), tagsEncoder.getCode(pair.second.first));
            if (pair.first.length() >= 4) {
                trieSuffixes.addWord(Utils.reverseString(pair.first.substring(pair.first.length() - 4)).toLowerCase(),
                        tagsEncoder.getCode(pair.second.first));
            }
            trieShapes.addWord(Utils.getShape(pair.first), tagsEncoder.getCode(pair.second.first));
        }
        ArrayList<Integer> tags = new ArrayList<Integer>();
        ArrayList<Integer> tags_ = new ArrayList<Integer>();
        ArrayList<Integer> tags2 = new ArrayList<Integer>();
        int tag;
        bioReader = new BIOReader(trainSmall);
        while ((pair = bioReader.getEntry()) != null) {
            if (pair.first.equals("")) {
                continue;
            }
            tags.add(tagsEncoder.getCode(pair.second.first));
            tags_.add(tagsEncoder.getCode(pair.second.second));
            tag = trie.getTag(pair.first.toLowerCase());
            if (tag < 0) {
                tag = trieSuffixes.getTag(Utils.reverseString(pair.first.substring(Math.max(0, pair.first.length() - 4))).toLowerCase());
                if (tag < 0) {
                    tag = trieShapes.getTag(Utils.getShape(pair.first));
                    if (tag < 0) {
                        System.out.println(pair.first + " " + pair.second);
                        tag = tagsEncoder.getCode("O");
                    }
                }
            }
            tags2.add(tag);
            if (tag != tags.get(tags.size() - 1)) {
                errMat[tags.get(tags.size() - 1)][tag]++;
            }
        }
        int[] myTags = new int[tags.size()], correctTags = new int[tags.size()], correctTags_ = new int[tags.size()];
        for (int i = 0; i < myTags.length; i++) {
            correctTags[i] = tags.get(i);
            correctTags_[i] = tags_.get(i);
            myTags[i] = tags2.get(i);
        }

        Transducer.N = 50;
        ArrayList<DeterministicTransducer> transducers = new ArrayList<DeterministicTransducer>();
        ArrayList<Rule> rulesTransducer = new ArrayList<Rule>();
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        for (char tag_from = 'a'; tag_from < errMat.length + 'a'; tag_from++) {
            for (char tag_to = 'a'; tag_to < errMat.length + 'a'; tag_to++) {
                if (errMat[tag_to - 'a'][tag_from - 'a'] == 0 && !(tag_from > 8 + 'a' && tag_to <= 8 + 'a')) {
                    continue;
                }
                for (char C = 'a'; C < errMat.length + 'a'; C++) {
                    if (tag_from > 8 + 'a' && tag_to <= 8 + 'a') {
                        indexes.add(transducers.size());
                    }
                    transducers.add(computeTransducer(0, tag_to, tag_from, C));
                    rulesTransducer.add(new Rule(0, tag_to, tag_from, C));
                    if (tag_from > 8 + 'a' && tag_to <= 8 + 'a') {
                        indexes.add(transducers.size());
                    }
                    transducers.add(computeTransducer(1, tag_to, C, tag_from));
                    rulesTransducer.add(new Rule(1, tag_to, C, tag_from));
                    for (char D = 'a'; D < errMat.length + 'a'; D++) {
                        if (tag_from > 8 + 'a' && tag_to <= 8 + 'a') {
                            indexes.add(transducers.size());
                        }
                        transducers.add(computeTransducer(0, tag_to, tag_from, C, D));
                        rulesTransducer.add(new Rule(0, tag_to, tag_from, C, D));
                        if (tag_from > 8 + 'a' && tag_to <= 8 + 'a') {
                            indexes.add(transducers.size());
                        }
                        transducers.add(computeTransducer(1, tag_to, C, tag_from, D));
                        rulesTransducer.add(new Rule(1, tag_to, C, tag_from, D));
                        if (tag_from > 8 + 'a' && tag_to <= 8 + 'a') {
                            indexes.add(transducers.size());
                        }
                        transducers.add(computeTransducer(2, tag_to, C, D, tag_from));
                        rulesTransducer.add(new Rule(2, tag_to, C, D, tag_from));
//                        for (char E = 'a'; E < errMat.length + 'a'; E++) {
//                            transducers.add(computeTransducer(0, tag_to, tag_from, C, D, E));
//                            rulesTransducer.add(new Rule(0, tag_to, tag_from, C, D, E));
//                            transducers.add(computeTransducer(1, tag_to, C, tag_from, D, E));
//                            rulesTransducer.add(new Rule(1, tag_to, C, tag_from, D, E));
//                            transducers.add(computeTransducer(2, tag_to, C, D, tag_from, E));
//                            rulesTransducer.add(new Rule(2, tag_to, C, D, tag_from, E));
//                            transducers.add(computeTransducer(3, tag_to, C, D, E, tag_from));
//                            rulesTransducer.add(new Rule(3, tag_to, C, D, E, tag_from));
//                            /*for (char F = 'a'; F < errMat.length + 'a'; F++) {
//                             errCnt = computeScore(correctTags, myTags, 0, tag_to, tag_from, C, D, E, F);
//                             if (errCnt > bestCnt) {
//                             bestRule = new Rule(0, tag_to, tag_from, C, D, E, F);
//                             bestCnt = errCnt;
//                             }
//                             errCnt = computeScore(correctTags, myTags, 1, tag_to, C, tag_from, D, E, F);
//                             if (errCnt > bestCnt) {
//                             bestRule = new Rule(1, tag_to, C, tag_from, D, E, F);
//                             bestCnt = errCnt;
//                             }
//                             errCnt = computeScore(correctTags, myTags, 2, tag_to, C, D, tag_from, E, F);
//                             if (errCnt > bestCnt) {
//                             bestRule = new Rule(2, tag_to, C, D, tag_from, E, F);
//                             bestCnt = errCnt;
//                             }
//                             errCnt = computeScore(correctTags, myTags, 3, tag_to, C, D, E, tag_from, F);
//                             if (errCnt > bestCnt) {
//                             bestRule = new Rule(3, tag_to, C, D, E, tag_from, F);
//                             bestCnt = errCnt;
//                             }
//                             errCnt = computeScore(correctTags, myTags, 4, tag_to, C, D, E, F, tag_from);
//                             if (errCnt > bestCnt) {
//                             bestRule = new Rule(4, tag_to, C, D, E, F, tag_from);
//                             bestCnt = errCnt;
//                             }
//                             }*/
//                        }
                    }
                }
            }
        }
        Transducer.N = 50000;
        System.out.println("# trans: " + transducers.size());
        System.out.println("indexes: " + indexes.size());

        ArrayList<Rule> rules = new ArrayList<Rule>(maxRules);
        int bestIndex[] = new int[maxRules];
        Rule bestRule;
        int errCnt, bestCnt, lastCnt = -1;
        for (int it = 0; it < maxRules; it++) {
            bestRule = null;
            bestCnt = Integer.MIN_VALUE;
            for (int i = 0; i < transducers.size(); i++) {
                errCnt = computeScore(correctTags, myTags, transducers.get(i));
                if (errCnt > bestCnt) {
                    bestRule = rulesTransducer.get(i);
                    bestCnt = errCnt;
                    bestIndex[it] = i;
                }
            }
            if (bestCnt > lastCnt) {
                lastCnt = bestCnt;
                rules.add(bestRule);
                myTags = transducers.get(bestIndex[it]).applyTransducer(myTags);
                for (int[] errMati : errMat) {
                    Arrays.fill(errMati, 0);
                }
                for (int i = 0; i < myTags.length; i++) {
                    if (myTags[i] != correctTags[i]) {
                        errMat[correctTags[i]][myTags[i]]++;
                    }
                }
                System.out.println("it " + it + " corrected: " + bestCnt);
            } else {
                break;
            }
            try {
                FileOutputStream fileOut
                        = new FileOutputStream(new File("./reglas"));
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(rules);
                out.close();
                fileOut.close();
            } catch (IOException i) {
                i.printStackTrace();
            }
        }
        lastCnt = 0;
        bestIndex = new int[indexes.size()];
        for (int it = 0; it < 30; it++) {
            bestRule = null;
            bestCnt = Integer.MIN_VALUE;
            for (int i = 0; i < indexes.size(); i++) {
                errCnt = computeScore(correctTags_, myTags, transducers.get(indexes.get(i)));
                if (errCnt > bestCnt) {
                    bestRule = rulesTransducer.get(indexes.get(i));
                    bestCnt = errCnt;
                    bestIndex[it] = indexes.get(i);
                }
            }
            if (bestCnt > lastCnt) {
                lastCnt = bestCnt;
                rules.add(bestRule);
                myTags = transducers.get(bestIndex[it]).applyTransducer(myTags);
                for (int[] errMati : errMat) {
                    Arrays.fill(errMati, 0);
                }
                for (int i = 0; i < myTags.length; i++) {
                    if (myTags[i] != correctTags[i]) {
                        errMat[correctTags[i]][myTags[i]]++;
                    }
                }
                System.out.println("it " + it + " corrected: " + bestCnt);
            } else {
                break;
            }
            try {
                FileOutputStream fileOut
                        = new FileOutputStream(new File("./reglas"));
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(rules);
                out.close();
                fileOut.close();
            } catch (IOException i) {
                i.printStackTrace();
            }
        }
        System.out.println("# rules: " + rules.size());
//
        Transducer t = Transducer.localExtension(rules.get(0).pat, rules.get(0).p, rules.get(0).trns, rules.get(0).pat.length);
        for (int i = 1; i < rules.size(); i++) {
            t = Transducer.compose(Transducer.localExtension(rules.get(i).pat, rules.get(i).p, rules.get(i).trns, rules.get(i).pat.length), t);
            System.out.println("i: " + i);
        }
        return new Model(trie, trieSuffixes, trieShapes, t.determinizeTransducer());
    }

    private static DeterministicTransducer computeTransducer(int trns, char p2, char... p1) {
        return Transducer.localExtension(p1, p2, trns, p1.length).determinizeTransducer();
    }

    private static int computeScore(int[] corTags, int[] myTags, DeterministicTransducer dt) {
        int myTags2[] = dt.applyTransducer(myTags);
        int score = 0;
        for (int i = 0; i < myTags2.length; i++) {
            if (myTags2[i] == corTags[i]) {
                score++;
            } else {
                score--;
            }
        }
        return score;
    }
}
