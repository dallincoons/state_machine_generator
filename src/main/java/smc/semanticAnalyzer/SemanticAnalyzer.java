package smc.semanticAnalyzer;

import smc.parser.FsmSyntax;

import java.util.*;

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
            } else if (isNamed(header, "actions")) {
                setHeader(actionsHeader, header);
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
        checkForUnusedStates(fsm);
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
                checkUndefinedState(superState, UNDEFINED_SUPER_STATE);
            }

            for (SubTransition st : t.subTransitions) {
                checkUndefinedState(st.nextState, UNDEFINED_STATE);
            }

            if (initialHeader.value != null && !semanticStateMachine.states.containsKey(initialHeader.value)) {
                semanticStateMachine.errors.add(new AnalysisError(UNDEFINED_STATE, "initial: " + initialHeader.value));
            }
        }
    }

    private void checkUndefinedState(String referencedState, AnalysisError.ID errorCode) {
        if (referencedState != null && !semanticStateMachine.states.containsKey(referencedState)) {
            semanticStateMachine.errors.add(new AnalysisError(errorCode, referencedState));
        }
    }

    private void checkForUnusedStates(FsmSyntax fsm)
    {
        findStatesDefinedButNotUsed(findUsedStates(fsm));
    }

    private Set<String> findUsedStates(FsmSyntax fsm) {
        Set<String> usedStates = new HashSet<>();
        usedStates.add(initialHeader.value);
        usedStates.addAll(getSuperStates(fsm));
        usedStates.addAll(getNextStates(fsm));
        return usedStates;
    }

    private void findStatesDefinedButNotUsed(Set<String> usedStates) {
        for (String definedState : semanticStateMachine.states.keySet())
            if (!usedStates.contains(definedState))
                semanticStateMachine.errors.add(new AnalysisError(UNUSED_STATE, definedState));
    }

    private Set<String> getNextStates(FsmSyntax fsm) {
        Set<String> nextStates = new HashSet<>();
        for (Transition t : fsm.logic)
            for (SubTransition st : t.subTransitions)
                if (st.nextState == null)
                    nextStates.add(t.state.name);
                else
                    nextStates.add(st.nextState);
        return nextStates;
    }

    private Set<String> getSuperStates(FsmSyntax fsm) {
        Set<String> superStates = new HashSet<>();
        for (Transition t : fsm.logic)
            for (String superState : t.state.superStates)
                superStates.add(superState);
        return superStates;
    }

    private void produceSemanticStateMachine(FsmSyntax fsm)
    {
        if (semanticStateMachine.errors.size() == 0) {
            compileHeaders();
            for (Transition t : fsm.logic) {
                SemanticState state = compileState(t);
                compileTransitions(t, state);
            }
        }

        new SuperClassCrawler().checkSuperClassTransitions();
    }

    private void compileHeaders() {
        semanticStateMachine.initialState = semanticStateMachine.states.get(initialHeader.value);
        semanticStateMachine.actionClass = actionsHeader.value;
        semanticStateMachine.fsmName = fsmHeader.value;
    }

    private SemanticState compileState(Transition t) {
        SemanticState state = semanticStateMachine.states.get(t.state.name);
        state.entryActions.addAll(t.state.entryActions);
        state.exitActions.addAll(t.state.exitActions);
        state.abstractState |= t.state.abstractState;
        for (String superStateName : t.state.superStates) {
            state.superStates.add(semanticStateMachine.states.get(superStateName));
        }
        return state;
    }

    private void compileTransitions(Transition t, SemanticState state) {
        for (SubTransition st : t.subTransitions) {
            compileTransition(state, st);
        }
    }

    private void compileTransition(SemanticState state, SubTransition st) {
        SemanticTransition semanticTransition = new SemanticTransition();
        semanticTransition.event = st.event;
        semanticTransition.nextState = st.nextState == null ? state : semanticStateMachine.states.get(st.nextState);
        semanticTransition.actions.addAll(st.actions);
        state.transitions.add(semanticTransition);
    }

    private SemanticState concreteState = null;
    private Map<String, SuperClassCrawler.TransitionTuple> transitionTuples;

    private class SuperClassCrawler {
        class TransitionTuple {
            String currentState;
            String event;
            String nextState;
            List<String> actions;

            TransitionTuple(String currentState, String event, String nextState, List<String> actions) {
                this.currentState = currentState;
                this.event = event;
                this.nextState = nextState;
                this.actions = actions;
            }

            public int hashCode() {
                return Objects.hash(currentState, event, nextState, actions);
            }

            public boolean equals(Object obj) {
                if (obj instanceof TransitionTuple) {
                    TransitionTuple tt = (TransitionTuple) obj;
                    return
                        Objects.equals(currentState, tt.currentState) &&
                        Objects.equals(event, tt.event) &&
                        Objects.equals(nextState, tt.nextState) &&
                        Objects.equals(actions, tt.actions);
                }
                return false;
            }
        }

        private void checkSuperClassTransitions() {
            for (SemanticState state : semanticStateMachine.states.values()) {
                if (state.abstractState == false) {
                    concreteState = state;
                    transitionTuples = new HashMap<>();
                    checkTransitionsForState(concreteState);
                }
            }
        }

        private void checkTransitionsForState(SemanticState state) {
            for (SemanticState superState : state.superStates)
                checkTransitionsForState(superState);
            checkStateForPreviouslyDefinedTransition(state);
        }

        private void checkStateForPreviouslyDefinedTransition(SemanticState state) {
            for (SemanticTransition st : state.transitions) {
                checkTransitionForPreviousDefinition(state, st);
            }
        }

        private void checkTransitionForPreviousDefinition(SemanticState state, SemanticTransition st) {
            TransitionTuple thisTuple = new TransitionTuple(state.name, st.event, st.nextState.name, st.actions);
            if (transitionTuples.containsKey(thisTuple.event)) {
                determineIfThePreviousDefinitionIsAnError(state, thisTuple);
            } else {
                transitionTuples.put(thisTuple.event, thisTuple);
            }
        }

        private void determineIfThePreviousDefinitionIsAnError(SemanticState state, TransitionTuple thisTuple) {
            TransitionTuple previousTuple = transitionTuples.get(thisTuple.event);
            if (!transitionsHaveSameOutcomes(thisTuple, previousTuple))
                checkForOverriddenTransition(state, thisTuple, previousTuple);
        }

        private boolean transitionsHaveSameOutcomes(TransitionTuple t1, TransitionTuple t2) {
            return
                Objects.equals(t1.nextState, t2.nextState) &&
                    Objects.equals(t1.actions, t2.actions);
        }

        private void checkForOverriddenTransition(SemanticState state, TransitionTuple thisTuple, TransitionTuple previousTuple) {
            SemanticState definingState = semanticStateMachine.states.get(previousTuple.currentState);
            if (!isSuperStateOf(definingState, state)) {
                semanticStateMachine.errors.add(new AnalysisError(CONFLICTING_SUPERSTATES, concreteState.name + "|" + thisTuple.event));
            } else {
                transitionTuples.put(thisTuple.event, thisTuple);
            }
        }

        private boolean isSuperStateOf(SemanticState possibleSuperState, SemanticState state) {
            if (state == possibleSuperState)
                return true;
            for (SemanticState superState : state.superStates)
                if (isSuperStateOf(possibleSuperState, superState))
                    return true;
            return false;
        }
    }
}
