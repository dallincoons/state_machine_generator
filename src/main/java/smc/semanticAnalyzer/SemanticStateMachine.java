package smc.semanticAnalyzer;

import java.util.*;

public class SemanticStateMachine {
    public List<AnalysisError> errors = new ArrayList<>();
    public SortedMap<String, SemanticState> states = new TreeMap<>();

    public void addError(AnalysisError analysisError) {
        errors.add(analysisError);
    }

    public static class AnalysisError {
        public enum ID {
            NO_FSM,
            NO_INITIAL,
            INVALID_HEADER,
            EXTRA_HEADER_IGNORED,
            UNDEFINED_STATE,
            UNDEFINED_SUPER_STATE,
            UNUSED_STATE,
            DUPLICATE_TRANSITION,
            ABSTRACT_STATE_USED_AS_NEXT_STATE,
            INCONSISTENT_ABSTRACTION,
            STATE_ACTIONS_MULTIPLY_DEFINED,
            CONFLICTING_SUPERSTATES,
        }

        private ID id;
        private Object extra;

        public AnalysisError(ID id) {
            this.id = id;
        }

        public AnalysisError(ID id, Object extra) {
            this(id);
            this.extra = extra;
        }

        public String toString() {
            return String.format("Semantic Error: %s(%s)", id.name(), extra);
        }

        public int hashCode() {
            return Objects.hash(id, extra);
        }

        public boolean equals(Object obj) {
            if (obj instanceof AnalysisError) {
                AnalysisError other = (AnalysisError) obj;
                return id == other.id && Objects.equals(extra, other.extra);
            }
            return false;
        }
    }

    public static class SemanticState implements Comparable<SemanticState> {
        public String name;

        public SemanticState(String name) {
            this.name = name;
        }

        public int compareTo(SemanticState s) {
            return name.compareTo(s.name);
        }
    }

    public static class SemanticTransition {
        public String event;
        public SemanticState nextState;
        public List<String> actions = new ArrayList<>();
    }
}
