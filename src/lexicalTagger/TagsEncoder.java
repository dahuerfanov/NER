package lexicalTagger;

import java.util.HashMap;

public class TagsEncoder {

    private HashMap<String, Integer> map;
    private String invMap[];
    public final int numberOfTags;

    public TagsEncoder(String tags[]) {
        map = new HashMap<String, Integer>(tags.length);
        invMap = new String[tags.length];
        for (int i = 0; i < tags.length; i++) {
            map.put(tags[i], i);
            invMap[i] = tags[i];
        }
        numberOfTags = tags.length;
    }

    public int getCode(String tag) {
        return map.get(tag);
    }

    public String getTag(int code) {
        return invMap[code];
    }
}
