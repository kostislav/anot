package cz.judas.jan.anot

import cz.judas.jan.anot.Token.Identifier
import cz.judas.jan.anot.Token.EmptyLine
import cz.judas.jan.anot.Token.Newline
import cz.judas.jan.anot.Token.StringValue
import cz.judas.jan.anot.Token.Symbol
import cz.judas.jan.anot.Token.Whitespace
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.io.path.Path
import kotlin.io.path.reader
import kotlin.test.assertEquals

class LexerTest {
    private lateinit var lexer: Lexer

    @BeforeEach
    fun setUp() {
        lexer = Lexer()
    }

    @Test
    fun parsesHelloWorld() {
        val input = Path("examples/projects/hello/src/hello.anot").reader()

        val parsedLines = lexer.parseTokens(input)

        assertThat(
            parsedLines, equalTo(
                listOf(
                    Identifier("import"), Whitespace(1), Symbol('{'), Whitespace(1), Symbol('/'), Identifier("stdlib"), Symbol('/'), Identifier("io"), Symbol('/'), Identifier("Stdio"), Whitespace(1), Symbol('}'), Newline,
                    EmptyLine,
                    Identifier("fun"), Whitespace(1), Identifier("hello"), Symbol('('), Identifier("stdio"), Symbol(':'), Whitespace(1), Identifier("Stdio"), Symbol(')'), Symbol(':'), Newline,
                    Whitespace(4), Identifier("stdio"), Symbol('.'), Identifier("println"), Symbol('('), StringValue("Hello, world"), Symbol(')'), Newline
                )
            )
        )
    }

    @Test
    fun failsForInvalidInput() {
        val input = "import ^eh".reader()

        val exception = assertThrows<RuntimeException> { lexer.parseTokens(input) }
        assertEquals(exception.message, "Unexpected character ^ on line 1 at position 8")
    }
}