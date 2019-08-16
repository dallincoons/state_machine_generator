package smc.generators.nestedSwitchCaseGenerator;

import java.util.ArrayList;
import java.util.List;

public interface NSCNode {
    public void accept(NSCNodeVisitor visitor);

    public static class SwitchCaseNode implements NSCNode {
        public String variableName;
        public List<NSCNode> caseNodes = new ArrayList<>();

        public SwitchCaseNode(String variableName) {
            this.variableName = variableName;
        }

        public void accept(NSCNodeVisitor visitor) {
            visitor.visit(this);
        }

        public void generateCases(NSCNodeVisitor visitor) {
            for (NSCNode node : caseNodes ) {
                node.accept(visitor);
            }
        }
    }

    public static class CaseNode implements NSCNode {
        public String caseName;
        public String currentState;
        public NSCNode caseActionNode;

        public CaseNode(String currentState, String caseName) {
            this.caseName = caseName;
            this.currentState = currentState;
        }

        public void accept(NSCNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    public class FSMClassNode implements NSCNode {
        public String className;
        public String actionsName;
        public EnumNode stateEnum;
        public EnumNode eventEnum;
        public StatePropertyNode stateProperty;
        public HandleEventNode handleEvent;
        public EventDelegatorsNode delegators;
        public List<String> actions;

        public void accept(NSCNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    public class EnumNode implements NSCNode {
        public String name;
        public List<String> enumerators;

        public EnumNode(String name, List<String> enumerators) {
            this.name = name;
            this.enumerators = enumerators;
        }

        public void accept(NSCNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    public class StatePropertyNode implements NSCNode {
        public String initialState;

        public StatePropertyNode(String initialState) {
            this.initialState = initialState;
        }

        @Override
        public void accept(NSCNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    public class EventDelegatorsNode implements NSCNode {
        public List<String> events;

        public EventDelegatorsNode(List<String> events) {
            this.events = events;
        }

        @Override
        public void accept(NSCNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    public class HandleEventNode implements NSCNode {
        public SwitchCaseNode switchCase;

        public HandleEventNode(SwitchCaseNode switchCase) {
            this.switchCase = switchCase;
        }

        public void accept(NSCNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    public class CompositeNode implements NSCNode {
        private List<NSCNode> nodes = new ArrayList<>();

        public void add(NSCNode node) {
            nodes.add(node);
        }

        public void accept(NSCNodeVisitor visitor) {
            for (NSCNode node : nodes) {
                node.accept(visitor);
            }
        }
    }

    public class FunctionCallNode implements NSCNode {
        public String name;
        public NSCNode argument;

        public FunctionCallNode(String name) {
            this.name = name;
        }

        public FunctionCallNode(String functionName, NSCNode argument) {
            this.name = functionName;
            this.argument = argument;
        }

        public void accept(NSCNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    public class EnumeratorNode implements NSCNode {
        public String enumeration;
        public String enumerator;

        public EnumeratorNode(String enumeration, String enumerator){
            this.enumeration = enumeration;
            this.enumerator = enumerator;
        }

        public void accept(NSCNodeVisitor visitor) {
            visitor.visit(this);
        }
    }

    public class DefaultCaseNode implements NSCNode {
        public String currentState;

        public DefaultCaseNode(String currentState) {
            this.currentState = currentState;
        }

        @Override
        public void accept(NSCNodeVisitor visitor) {
            visitor.visit(this);
        }
    }
}
