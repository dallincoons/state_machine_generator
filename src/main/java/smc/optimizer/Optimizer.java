package smc.optimizer;

import smc.OptimizedStateMachine;
import smc.semanticAnalyzer.SemanticStateMachine;

import java.util.*;

import static smc.OptimizedStateMachine.*;
import static smc.semanticAnalyzer.SemanticStateMachine.SemanticState;
import static smc.semanticAnalyzer.SemanticStateMachine.SemanticTransition;

public class Optimizer {
    private OptimizedStateMachine optimizedStateMachine;
    private SemanticStateMachine semanticStateMachine;

    public OptimizedStateMachine optimize(SemanticStateMachine ast) {
        semanticStateMachine = ast;
        optimizedStateMachine = new OptimizedStateMachine();
        addHeader(ast);
        return optimizedStateMachine;
    }

    private void addHeader(SemanticStateMachine ast) {
        optimizedStateMachine.header = new Header();
    }

    private class SubTransitionOptimizer {
        private SemanticTransition semanticTransition;
    }
}
