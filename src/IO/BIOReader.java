package IO;

import utils.Pair;

import java.io.*;

public class BIOReader implements TrainingReader {

    private final BufferedReader in;
    private String toks[], line;
    private static final String accent = "áàäéèëíìïóòöúùüÁÀÄÉÈËÍÌÏÓÒÖÚÙÜçÇ";
    private static final String correct = "aaaeeeiiiooouuuAAAEEEIIIOOOUUUcC";

    public BIOReader(File file) throws FileNotFoundException, UnsupportedEncodingException {
        in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
    }

    @Override
    public Pair<String, Pair<String, String>> getEntry() throws IOException {
        line = in.readLine();
        if (line == null) {
            return null;
        }
        toks = line.split("[\\s]+");

        if (toks[0].toLowerCase().equals("en")) {
            return new Pair<String, Pair<String, String>>(toks[0], new Pair<String, String>("EN", toks[1]));
        }
        if (toks[0].toLowerCase().equals("de")) {
            return new Pair<String, Pair<String, String>>(toks[0], new Pair<String, String>("DE", toks[1]));
        }
        if (toks[0].toLowerCase().equals("por")) {
            return new Pair<String, Pair<String, String>>(toks[0], new Pair<String, String>("POR", toks[1]));
        }
        if (toks[0].toLowerCase().equals("según") || toks[0].toLowerCase().equals("segun")) {
            return new Pair<String, Pair<String, String>>(toks[0], new Pair<String, String>("SEGUN", toks[1]));
        }

        if (toks[0].length() == 1 && (toks[0].equals(".") || toks[0].equals(",") || toks[0].equals(";") || toks[0].equals("\"")
                || toks[0].equals(":") || toks[0].equals("'") || toks[0].equals("(") || toks[0].equals(")")
                || toks[0].equals("[") || toks[0].equals("]") || toks[0].equals("{") || toks[0].equals("}")
                || toks[0].equals("!") || toks[0].equals("¡") || toks[0].equals("?") || toks[0].equals("¿"))) {
            return new Pair<String, Pair<String, String>>(toks[0], new Pair<String, String>("PUNCTUATION", "PUNCTUATION"));
        }
        StringBuilder sb = new StringBuilder();
        char c;
        int aux;
        for (int i = 0; i < toks[0].length(); i++) {
            c = toks[0].charAt(i);
            if ((aux = accent.indexOf(c)) >= 0) {
                sb.append(correct.charAt(aux));
            } else {
                sb.append(c);
            }
        }
        return new Pair<String, Pair<String, String>>(sb.toString(), new Pair<String, String>(toks.length == 2 ? toks[1] : "<empty line>", toks.length == 2 ? toks[1] : "<empty line>"));
    }
}
