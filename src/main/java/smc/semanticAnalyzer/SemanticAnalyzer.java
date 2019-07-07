package smc.semanticAnalyzer;

import smc.parser.FsmSyntax;
import static smc.parser.FsmSyntax.*;
import static smc.semanticAnalyzer.SemanticStateMachine.*;
import static smc.semanticAnalyzer.SemanticStateMachine.AnalysisError.ID.*;

public class SemanticAnalyzer {
    private SemanticStateMachine semanticStateMachine;
    private FsmSyntax.Header fsmHeader = Header.NullHeader();
    private Header actionsHeader  = Header.NullHeader();
    private Header initialHeader = Header.NullHeader();

    public SemanticStateMachine analyze(FsmSyntax fsm)
    {
        semanticStateMachine = new SemanticStateMachine();
        analyzeHeaders(fsm);
        checkSemanticValidity(fsm);
        produceSemanticStateMachine(fsm);
        return semanticStateMachine;
    }

    private void analyzeHeaders(FsmSyntax fsm)
    {
        setHeaders(fsm);
        checkMissingHeaders();
    }

    private void setHeaders(FsmSyntax fsm)
    {
        for (FsmSyntax.Header header : fsm.headers) {
            if(isNamed(header, "fsm")) {
                setHeader(fsmHeader, header);
            } else if (isNamed(header, "initial")) {
                setHeader(initialHeader, header);
            } else {
                semanticStateMachine.addError(new AnalysisError(INVALID_HEADER, header));
            }
        }
    }

    private boolean isNamed(FsmSyntax.Header header, String headerName)
    {
        return header.name.equalsIgnoreCase(headerName);
    }

    private void setHeader(Header targetHeader, FsmSyntax.Header header) {
        if (isNullHeader(targetHeader)) {
            targetHeader.name = header.name;
            targetHeader.value = header.value;
        } else {
            semanticStateMachine.addError(new AnalysisError(EXTRA_HEADER_IGNORED, header));
        }
    }

    private void checkMissingHeaders()
    {
        if (isNullHeader(fsmHeader)) {
            semanticStateMachine.addError(new AnalysisError(NO_FSM));
        }
        if (isNullHeader(initialHeader)) {
            semanticStateMachine.addError(new AnalysisError(NO_INITIAL));
        }
    }

    private boolean isNullHeader(Header header) {
        return header.name == null;
    }

    private void checkSemanticValidity(FsmSyntax fsm)
    {
        createStateEventAndActionLists(fsm);
        checkUndefinedStates(fsm);
    }

    private void createStateEventAndActionLists(FsmSyntax fsm) {
        addStateNamesToStateList(fsm);
//        addEntryAndExitActionsToActionList(fsm);
//        addEventsToEventList(fsm);
//        addTransitionActionsToActionList(fsm);
    }

    private void addStateNamesToStateList(FsmSyntax fsm) {
        for (Transition t : fsm.logic) {
            SemanticState state = new SemanticState(t.state.name);
            semanticStateMachine.states.put(state.name, state);
        }
    }

    private void checkUndefinedStates(FsmSyntax fsm)
    {
        for (Transition t : fsm.logic) {
            for (String superState : t.state.superStates) {
//                checkUndefinedStates(superState, Un);
            }

            for (SubTransition st : t.subTransitions) {

            }

            if (initialHeader.value != null && !semanticStateMachine.states.containsKey(initialHeader.value)) {
                semanticStateMachine.errors.add(new AnalysisError(UNDEFINED_STATE, "initial: " + initialHeader.value));
            }
        }
    }

    private void produceSemanticStateMachine(FsmSyntax fsm)
    {

    }
}
