package smc.semanticAnalyzer;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import smc.lexer.Lexer;
import smc.parser.Builder;
import smc.parser.Parser;
import smc.parser.SyntaxBuilder;

import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
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
    }
}
