package contextualTagger;

import static contextualTagger.LearningManager.tagsEncoder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import utils.Pair;
import utils.Utils;

public class ApplicationManager {

    public static List<Pair<String, String>> applyModel(File file, Model model) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        ArrayList<Integer> tags = new ArrayList<Integer>();
        ArrayList<String> words = new ArrayList<String>();
        int tag;
        String line, toks[];
        while ((line = in.readLine()) != null) {
            toks = line.split("[\\s]+");
            for (String tok : toks) {
                words.add(tok);
                tag = model.trie.getTag(tok.toLowerCase());
                if (tag < 0) {
                    tag = model.trieSuffixes.getTag(Utils.reverseString(tok.substring(Math.max(0, tok.length() - 4))).toLowerCase());
                    if (tag < 0) {
                        tag = model.trieShapes.getTag(Utils.getShape(tok));
                        if (tag < 0) {
                            tag = tagsEncoder.getCode("O");
                        }
                    }
                }
                tags.add(tag);
            }
        }
        int[] myTags = new int[tags.size()];
        for (int i = 0; i < myTags.length; i++) {
            myTags[i] = tags.get(i);
        }
        myTags = model.dt.applyTransducer(myTags);
        List<Pair<String, String>> result = new ArrayList<Pair<String, String>>(myTags.length);
        for (int i = 0; i < myTags.length; i++) {
            result.add(new Pair<String, String>(words.get(i), tagsEncoder.getTag(myTags[i])));
        }
        return result;
    }
}
