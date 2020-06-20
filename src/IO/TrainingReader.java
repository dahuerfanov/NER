package IO;

import utils.Pair;

import java.io.IOException;

public interface TrainingReader {

    Pair<String, Pair<String, String>> getEntry() throws IOException;
}
