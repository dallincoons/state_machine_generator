package smc.semanticAnalyzer;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import smc.lexer.Lexer;
import smc.parser.Parser;
import smc.parser.SyntaxBuilder;

import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static smc.parser.FsmSyntax.Header;
import static smc.parser.ParserEvent.EOF;
import static smc.semanticAnalyzer.SemanticStateMachine.AnalysisError;
import static smc.semanticAnalyzer.SemanticStateMachine.AnalysisError.ID.*;

@RunWith(HierarchicalContextRunner.class)
public class semanticAnalyzerTest {
    private Lexer lexer;
    private Parser parser;
    private SyntaxBuilder builder;
    private SemanticAnalyzer analyzer;

    @Before
    public void setUp()
    {
        builder = new SyntaxBuilder();
        parser = new Parser(builder);
        lexer = new Lexer(parser);
        analyzer = new SemanticAnalyzer();
    }

    private SemanticStateMachine produceAst(String s) {
        lexer.lex(s);
        parser.handleEvent(EOF, -1, -1);
        return analyzer.analyze(builder.getFsm());
    }

    public class SemanticErrors {
        public class HeaderErrors {
            @Test
            public void noHeaders() throws Exception {
                List<AnalysisError> errors = produceAst("{}").errors;
                assertThat(errors, hasItems(
                    new AnalysisError(NO_FSM),
                    new AnalysisError(NO_INITIAL)
                ));
            }

            @Test
            public void missingActions() throws Exception {
                List<AnalysisError> errors = produceAst("FSM:f Initial:i {}").errors;
                assertThat(errors, not(hasItems(
                        new AnalysisError(NO_FSM),
                        new AnalysisError(NO_INITIAL))
                    ));
            }

            @Test
            public void missingFsm() throws Exception {
                List<AnalysisError> errors = produceAst("actions:a Initial:i {}").errors;
                assertThat(errors, not(hasItems(
                        new AnalysisError(NO_INITIAL))
                ));
                assertThat(errors, hasItems(new AnalysisError(NO_FSM)));
            }

            @Test
            public void missingInitial() throws Exception {
                List<AnalysisError> errors = produceAst("actions:a Fsm:f {}").errors;
                assertThat(errors, not(hasItems(
                        new AnalysisError(NO_FSM))
                ));
                assertThat(errors, hasItems(new AnalysisError(NO_INITIAL)));
            }

            @Test
            public void nothingMissing() throws Exception {
                List<AnalysisError> errors = produceAst("Initial: f Actions:a Fsm:f {}").errors;
                assertThat(errors, not(hasItems(
                    new AnalysisError(NO_FSM),
                    new AnalysisError(NO_INITIAL)
                )));
            }

            @Test
            public void unexpectedHeader() throws Exception {
                List<AnalysisError> errors = produceAst("X: x{s - - -}").errors;
                assertThat(errors, hasItems(
                    new AnalysisError(INVALID_HEADER, new Header("X", "x"))
                ));
            }

            @Test
            public void duplicateHeader() throws Exception {
                List<AnalysisError> errors = produceAst("fsm:f fsm:x{s - - -}").errors;
                assertThat(errors, hasItems(
                        new AnalysisError(EXTRA_HEADER_IGNORED, new Header("fsm", "x"))
                ));
            }

            @Test
            public void initialStateMustBeDefined() throws Exception {
                List<AnalysisError> errors = produceAst("initial: i {s - - -}").errors;
                assertThat(errors, hasItems(
                    new AnalysisError(UNDEFINED_STATE, "initial: i")
                ));
            }
        }

        public class StateErrors {
            @Test
            public void nullNextStateIsNotUndefined() throws Exception {
                List<AnalysisError> errors = produceAst("{s - - -}").errors;
                assertThat(errors, not(hasItems(
                    new AnalysisError(UNDEFINED_STATE, null)
                )));
            }

            @Test
            public void undefinedState() throws Exception {
                List<AnalysisError> errors = produceAst("{s - s2 -}").errors;
                assertThat(errors, hasItems(
                    new AnalysisError(UNDEFINED_STATE, "s2")
                ));
            }

            @Test
            public void noUndefinedState() throws Exception {
                List<AnalysisError> errors = produceAst("{s - s -}").errors;
                assertThat(errors, not(hasItems(
                    new AnalysisError(UNDEFINED_STATE)
                )));
            }

            @Test
            public void noUndefinedStates() throws Exception {
                List<AnalysisError> errors = produceAst("{s - s -}").errors;
                assertThat(errors, not(hasItems(new AnalysisError(UNDEFINED_STATE, "s2"))));
            }

            @Test
            public void undefinedSuperState() throws Exception {
                List<AnalysisError> errors = produceAst("{s:ss - - -}").errors;
                assertThat(errors, hasItems(new AnalysisError(UNDEFINED_SUPER_STATE, "ss")));
            }

            @Test
            public void superStateDefined() throws Exception {
                List<AnalysisError> errors = produceAst("{ss - - - s:ss - - -}").errors;
                assertThat(errors, not(hasItems(new AnalysisError(UNDEFINED_SUPER_STATE, "s2"))));
            }

            @Test
            public void unusedStates() throws Exception {
                List<AnalysisError> errors = produceAst("{s e n -}").errors;
                assertThat(errors, hasItems(new AnalysisError(UNUSED_STATE, "s")));
            }

            @Test
            public void noUnusedStates() throws Exception {
                List<AnalysisError> errors = produceAst("{s e s -}").errors;
                assertThat(errors, not(hasItems(new AnalysisError(UNUSED_STATE, "s"))));
            }

            @Test
            public void nextStateNullIsImplicitUsed() throws Exception {
                List<AnalysisError> errors = produceAst("{s e - -}").errors;
                assertThat(errors, not(hasItems(new AnalysisError(UNUSED_STATE, "s"))));
            }

            @Test
            public void usedAsBaseIsValidUsage() throws Exception {
                List<AnalysisError> errors = produceAst("{b e n - s:b e2 s -}").errors;
                assertThat(errors, not(hasItems(new AnalysisError(UNUSED_STATE, "b"))));
            }

            @Test
            public void usedAsInitialIsValidUsage() throws Exception {
                List<AnalysisError> errors = produceAst("initial: b {b e n -}").errors;
                assertThat(errors, not(hasItems(new AnalysisError(UNUSED_STATE, "b"))));
            }

            @Test
            public void errorIfSuperStatesHaveConflictingTransitions() throws Exception {
                List<AnalysisError> errors = produceAst(
                     "" +
                        "FSM: f Actions: act Initial: s" +
                        "{" +
                        "  (ss1) e1 s1 -" +
                        "  (ss2) e1 s2 -" +
                        "  s :ss1 :ss2 e2 s3 a" +
                        "  s2 e s -" +
                        "  s1 e s -" +
                        "  s3 e s -" +
                        "}").errors;
                assertThat(errors, hasItems(new AnalysisError(CONFLICTING_SUPERSTATES, "s|e1")));
            }

            @Test
            public void noErrorForOverriddenTransition() throws Exception {
                List<AnalysisError> errors = produceAst(
                        "" +
                        "FSM: f Actions: act Initial: s" +
                        "{" +
                        "  (ss1) e1 s1 -" +
                        "  s :ss1 e1 s3 a" +
                        "  s1 e s -" +
                        "  s3 e s -" +
                        "}").errors;
                assertThat(errors, not(hasItems(new AnalysisError(CONFLICTING_SUPERSTATES, "s|e1"))));
            }

            @Test
            public void noErrorIfSuperStatesHaveIdenticalTransitions() throws Exception {
                List<AnalysisError> errors = produceAst(
                        "" +
                                "FSM: f Actions: act Initial: s" +
                                "{" +
                                "  (ss1) e1 s1 ax" +
                                "  (ss2) e1 s1 ax" +
                                "  s :ss1 :ss2 e2 s3 a" +
                                "  s1 e s -" +
                                "  s3 e s -" +
                                "}").errors;
                assertThat(errors, not(hasItems(new AnalysisError(CONFLICTING_SUPERSTATES, "s|e1"))));
            }

            @Test
            public void errorIfSuperstatesHaveDifferentActionsInSameTransitions() throws Exception {
                List<AnalysisError> errors = produceAst(
                        "" +
                                "FSM: f Actions: act Initial: s" +
                                "{" +
                                "  (ss1) e1 s1 a1" +
                                "  (ss2) e1 s1 a2" +
                                "  s :ss1 :ss2 e2 s3 a" +
                                "  s1 e s -" +
                                "  s3 e s -" +
                                "}").errors;
                assertThat(errors, hasItems(new AnalysisError(CONFLICTING_SUPERSTATES, "s|e1")));

            }
        } // State Errors

        public class TransitionErrors {
            @Test
            public void duplicateTransitions() throws Exception {
                List<AnalysisError> errors = produceAst("{s e - - s e - -}").errors;
                assertThat(errors, hasItems(new AnalysisError(DUPLICATE_TRANSITION, "s(e)")));
            }

            @Test
            public void noDuplicateTransitions() throws Exception {
                List<AnalysisError> errors = produceAst("{s e - -}").errors;
                assertThat(errors, not(hasItems(new AnalysisError(DUPLICATE_TRANSITION, "s(e)"))));
            }

            @Test
            public void abstractStatesCantBeTargets() throws Exception {
                List<AnalysisError> errors = produceAst("{(as) e - - s e as -}").errors;
                assertThat(errors, hasItems(new AnalysisError(ABSTRACT_STATE_USED_AS_NEXT_STATE, "s(e)->as")));
            }

            @Test
            public void abstractStatesCanBeUsedAsSuperStates() throws Exception {
                List<AnalysisError> errors = produceAst("{(as) e - - s:as e s -}").errors;
                assertThat(errors, not(hasItems(new AnalysisError(ABSTRACT_STATE_USED_AS_NEXT_STATE, "s(e)->s"))));
            }

            @Test
            public void entryAndExitActionsNotMultiplyDefined() throws Exception {
                List<AnalysisError> errors = produceAst(
                        "" +
                                "{" +
                                "  s - - - " +
                                "  s - - -" +
                                "  es - - -" +
                                "  es <x - - - " +
                                "  es <x - - -" +
                                "  xs >x - - -" +
                                "  xs >{x} - - -" +
                                "}").errors;
                assertThat(errors, not(hasItems(new AnalysisError(STATE_ACTIONS_MULTIPLY_DEFINED, "s"))));
                assertThat(errors, not(hasItems(new AnalysisError(STATE_ACTIONS_MULTIPLY_DEFINED, "es"))));
                assertThat(errors, not(hasItems(new AnalysisError(STATE_ACTIONS_MULTIPLY_DEFINED, "xs"))));
            }

            @Test
            public void errorIfStateHasMultipleEntryActionDefinitions() throws Exception {
                List<AnalysisError> errors = produceAst("{s - - - ds <x - - - ds <y - - -}").errors;
                assertThat(errors, not(hasItems(new AnalysisError(STATE_ACTIONS_MULTIPLY_DEFINED, "s"))));
                assertThat(errors, hasItems(new AnalysisError(STATE_ACTIONS_MULTIPLY_DEFINED, "ds")));
            }

            @Test
            public void errorIfStateHasMultipleExitActionDefinitions() throws Exception {
                List<AnalysisError> errors = produceAst("{ds >x - - - ds >y - -}").errors;
                assertThat(errors, hasItems(new AnalysisError(STATE_ACTIONS_MULTIPLY_DEFINED, "ds")));
            }
        } // Transition Errors
    } // Semantic Errors

    public class Warnings {
        @Test
        public void warnIfStateUsedAsBothAbstractAndConcrete() throws Exception {
            List<AnalysisError> errors = produceAst("{(ias) e - - ias e - - (cas) e - -}").warnings;
            assertThat(errors, not(hasItems(new AnalysisError(INCONSISTENT_ABSTRACTION, "cas"))));
            assertThat(errors, hasItems(new AnalysisError(INCONSISTENT_ABSTRACTION, "ias")));
        }
    } // Warnings
}
