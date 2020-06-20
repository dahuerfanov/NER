package contextualTagger;

import IO.BIOReader;
import IO.TrainingReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import lexicalTagger.TagsEncoder;
import utils.Pair;
import utils.Utils;
import static contextualTagger.LearningManager.*;

public class TestingManager {

    public static void test(File fileTest, Model model) throws IOException {
        TrainingReader bioReader = new BIOReader(fileTest);
        ArrayList<Integer> tags = new ArrayList<Integer>();
        ArrayList<Integer> tags2 = new ArrayList<Integer>();
        Pair<String, Pair<String, String>> p;
        int tag;
        while ((p = bioReader.getEntry()) != null) {
            if (p.first.equals("")) {
                continue;
            }
            tags.add(tagsEncoder.getCode(p.second.second));
            tag = model.trie.getTag(p.first.toLowerCase());
            if (tag < 0) {
                tag = model.trieSuffixes.getTag(Utils.reverseString(p.first.substring(Math.max(0, p.first.length() - 4))).toLowerCase());
                if (tag < 0) {
                    tag = model.trieShapes.getTag(Utils.getShape(p.first));
                    if (tag < 0) {
                        System.out.println(p.first + " " + p.second.second);
                        tag = tagsEncoder.getCode("O");
                    }
                }
            }
            tags2.add(tag);
        }

        int[] corTags = new int[tags.size()];
        int[] myTags = new int[tags2.size()];
        for (int i = 0; i < myTags.length; i++) {
            corTags[i] = tags.get(i);
            myTags[i] = tags2.get(i);
        }
        System.out.println("**************************");
        computePerformance(tagsEncoder, corTags, myTags);
        System.out.println("\n\nAfter Transducer\n\n");
        myTags = model.dt.applyTransducer(myTags);
        System.out.println("**************************");
        computePerformance(tagsEncoder, corTags, myTags);
        System.out.println("\n\nautomaton size: " + model.dt.size());
    }

    private static void computePerformance(TagsEncoder tagsEncoder,
            int corTags[], int myTags[]) {
        HashSet<Pair<Pair<Integer, Integer>, Integer>> corEntities = getMap(tagsEncoder, corTags);
        HashSet<Pair<Pair<Integer, Integer>, Integer>> myEntities = getMap(tagsEncoder, myTags);
        double tp = 0, fp = 0, fn;
        for (Pair<Pair<Integer, Integer>, Integer> pair : myEntities) {
            if (corEntities.contains(pair)) {
                tp++;
            } else {
                fp++;
            }
        }
        fn = corEntities.size() - tp;
        System.out.printf("Recall: %.3f  Precision: %.3f  F1: %.3f\n",
                (tp / (tp + fn)), (tp / (tp + fp)),
                2. * (tp / (tp + fn)) * (tp / (tp + fp)) / ((tp / (tp + fn)) + (tp / (tp + fp))));
    }

    private static HashSet<Pair<Pair<Integer, Integer>, Integer>> getMap(TagsEncoder tagsEncoder, int[] tags) {
        int type = -1, beginIndex = -1;
        HashSet<Pair<Pair<Integer, Integer>, Integer>> entities = new HashSet<Pair<Pair<Integer, Integer>, Integer>>();
        for (int i = 0; i < tags.length; i++) {//"B-PER", "B-LOC", "B-ORG", "B-MISC"
            if (tags[i] == tagsEncoder.getCode("B-PER")) {
                if (type >= 0) {
                    entities.add(new Pair<Pair<Integer, Integer>, Integer>(new Pair<Integer, Integer>(beginIndex, i - 1), type));
                }
                type = 0;
                beginIndex = i;
            } else if (tags[i] == tagsEncoder.getCode("B-LOC")) {
                if (type >= 0) {
                    entities.add(new Pair<Pair<Integer, Integer>, Integer>(new Pair<Integer, Integer>(beginIndex, i - 1), type));
                }
                type = 1;
                beginIndex = i;
            } else if (tags[i] == tagsEncoder.getCode("B-ORG")) {
                if (type >= 0) {
                    entities.add(new Pair<Pair<Integer, Integer>, Integer>(new Pair<Integer, Integer>(beginIndex, i - 1), type));
                }
                type = 2;
                beginIndex = i;
            } else if (tags[i] == tagsEncoder.getCode("B-MISC")) {
                if (type >= 0) {
                    entities.add(new Pair<Pair<Integer, Integer>, Integer>(new Pair<Integer, Integer>(beginIndex, i - 1), type));
                }
                type = 3;
                beginIndex = i;
            } else if (tags[i] == tagsEncoder.getCode("I-PER")) {
                if (type >= 0) {
                    if (type != 0) {
                        //entities.add(new Pair<Pair<Integer, Integer>, Integer>(new Pair<Integer, Integer>(beginIndex, i - 1), type));
                        //type = -1;
                    }
                }
            } else if (tags[i] == tagsEncoder.getCode("I-LOC")) {
                if (type >= 0) {
                    if (type != 1) {
                        //entities.add(new Pair<Pair<Integer, Integer>, Integer>(new Pair<Integer, Integer>(beginIndex, i - 1), type));
                        //type = -1;
                    }
                }
            } else if (tags[i] == tagsEncoder.getCode("I-ORG")) {
                if (type >= 0) {
                    if (type != 2) {
                        //entities.add(new Pair<Pair<Integer, Integer>, Integer>(new Pair<Integer, Integer>(beginIndex, i - 1), type));
                        //type = -1;
                    }
                }
            } else if (tags[i] == tagsEncoder.getCode("I-MISC")) {
                if (type >= 0) {
                    if (type != 3) {
                        //entities.add(new Pair<Pair<Integer, Integer>, Integer>(new Pair<Integer, Integer>(beginIndex, i - 1), type));
                        //type = -1;
                    }
                }
            } else {
                if (type >= 0) {
                    entities.add(new Pair<Pair<Integer, Integer>, Integer>(new Pair<Integer, Integer>(beginIndex, i - 1), type));
                }
                type = -1;
            }
        }
        return entities;
    }
}
