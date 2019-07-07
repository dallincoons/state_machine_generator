package smc.optimizer;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import smc.OptimizedStateMachine;
import smc.lexer.Lexer;
import smc.parser.SyntaxBuilder;
import smc.parser.Parser;
import smc.semanticAnalyzer.SemanticAnalyzer;
import smc.semanticAnalyzer.SemanticStateMachine;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static smc.parser.ParserEvent.EOF;

@RunWith(HierarchicalContextRunner.class)
public class OptimizerTest {
    private SyntaxBuilder builder;
    private Parser parser;
    private Lexer lexer;
    private SemanticAnalyzer analyzer;
    private Optimizer optimizer;

    @Before
    public void setup() throws Exception {
        builder = new SyntaxBuilder();
        parser = new Parser(builder);
        lexer = new Lexer(parser);
        analyzer = new SemanticAnalyzer();
        optimizer = new Optimizer();
    }

    private OptimizedStateMachine produceStateMachineWithHeader(String s)
    {
        String fsmSyntax = "fsm: f initial:i actions:a" + s;
        return produceStateMachine(fsmSyntax);
    }

    private OptimizedStateMachine produceStateMachine(String fsmSyntax)
    {
        lexer.lex(fsmSyntax);
        parser.handleEvent(EOF, -1, -1);
        SemanticStateMachine ast = analyzer.analyze(builder.getFsm());
        return optimizer.optimize(ast);
    }

    public class BasicOptimizerFunctions {
        @Test
        public void header() throws Exception {
            OptimizedStateMachine sm = produceStateMachineWithHeader("{i e i -}");
//            assertThat(sm.header.fsm, equalTo("f"));
        }
    }
}
