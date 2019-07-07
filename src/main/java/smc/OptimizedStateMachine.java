package smc;

import java.util.ArrayList;
import java.util.List;

public class OptimizedStateMachine {
    public List<String> states = new ArrayList<>();
    public Header header;

    public static class Header {
        public String initial;
        public String fsm;
        public String action;
    }
}
