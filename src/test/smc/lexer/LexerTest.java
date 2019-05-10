package smc.lexer;

import org.junit.Before;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import de.bechte.junit.runners.context.HierarchicalContextRunner;
import org.junit.runner.RunWith;

@RunWith(HierarchicalContextRunner.class)
public class LexerTest implements TokenCollector {
    String tokens = "";
    Lexer lexer;
    private boolean firstToken = true;

    @Before
    public void setUp() throws Exception {
        lexer = new Lexer(this);
    }

    private void addToken(String token) {
        if (!firstToken)
            tokens += ",";
        tokens += token;
        firstToken = false;
    }

    private void assertLexResult(String input, String expected) {
        lexer.lex(input);
        assertEquals(expected, tokens);
    }

    public void openBrace(int line, int pos) {
        addToken("OB");
    }

    public void closeBrace(int line, int pos) {
        addToken("CB");
    }

    public void openParen(int line, int pos) {
        addToken("OP");
    }

    public void closeParen(int line, int pos) {
        addToken("CP");
    }

    public void openAngle(int line, int pos) {
        addToken("OA");
    }

    public void closeAngle(int line, int pos) {
        addToken("CA");
    }

    public void dash(int line, int pos) {
        addToken("D");
    }

    @Override
    public void colon(int line, int pos) {
        addToken("C");
    }

    public void name(String name, int line, int pos) {
        addToken("#" + name + "#");
    }

    public void error(int line, int pos) {
        addToken("E" + line + "/" + pos);
    }

    public class SingleTokenTests {
        @Test
        public void findsOpenBrace() throws Exception {
            assertLexResult("{", "OB");
        }

        @Test
        public void findsClosedBrace() throws Exception {
            assertLexResult("}", "CB");
        }

        @Test
        public void findsOpenParen() throws Exception {
            assertLexResult("(", "OP");
        }

        @Test
        public void findsClosedParen() throws Exception {
            assertLexResult(")", "CP");
        }

        public void findsOpenAngle() throws Exception {
            assertLexResult("<", "OA");
        }

        public void findsClosedAngle() throws Exception {
            assertLexResult(">", "CA");
        }

        @Test
        public void findsDash() throws Exception {
            assertLexResult("-", "D");
        }

        @Test
        public void findsSimpleName() throws Exception {
            assertLexResult("name", "#name#");
        }

        @Test
        public void findComplexName() throws Exception {
            assertLexResult("Room_222", "#Room_222#");
        }

        @Test
        public void error() throws Exception {
            assertLexResult(".", "E1/1");
        }

        @Test
        public void nothingButWhiteSpace() throws Exception {
            assertLexResult(" ", "");
        }

        @Test
        public void whiteSpaceBefore() throws Exception {
            assertLexResult("  \t\n  -", "D");
        }
    }
}
