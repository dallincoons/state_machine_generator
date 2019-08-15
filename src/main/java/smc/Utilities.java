package smc;

public class Utilities {
    public static String compressWhiteSpace(String s) {
        return s.replaceAll("\\n+", "\n")
            .replaceAll("[\t ]+", " ")
            .replaceAll(" *\n *", "\n");
    }
}
