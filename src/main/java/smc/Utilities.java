package smc;

import java.util.List;

public class Utilities {
    public static String compressWhiteSpace(String s) {
        return s.replaceAll("\\n+", "\n")
            .replaceAll("[\t ]+", " ")
            .replaceAll(" *\n *", "\n");
    }

    public static String commaList(List<String> list) {
        String output = "";
        boolean first = true;
        for (String s : list) {
           output += (first ? "" : ",") + s;
           first = false;
        }
        return output;
    }
}
