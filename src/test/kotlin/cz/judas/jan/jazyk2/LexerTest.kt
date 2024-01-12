package cz.judas.jan.jazyk2

import cz.judas.jan.jazyk2.Token.Alphanumeric
import cz.judas.jan.jazyk2.Token.EmptyLine
import cz.judas.jan.jazyk2.Token.Newline
import cz.judas.jan.jazyk2.Token.StringValue
import cz.judas.jan.jazyk2.Token.Symbol
import cz.judas.jan.jazyk2.Token.Whitespace
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
        val input = Path("examples/hello/src/hello.jaz").reader()

        val parsedLines = lexer.parseTokens(input)

        assertThat(
            parsedLines, equalTo(
                listOf(
                    Alphanumeric("import"), Symbol(':'), Newline,
                    Whitespace(4), Symbol('/'), Alphanumeric("stdlib"), Symbol('/'), Alphanumeric("io"), Symbol('/'), Alphanumeric("println"), Newline,
                    Whitespace(4), Symbol('/'), Alphanumeric("stdlib"), Symbol('/'), Alphanumeric("entrypoint"), Newline,
                    EmptyLine,
                    Symbol('@'), Alphanumeric("entrypoint"), Newline,
                    Alphanumeric("def"), Whitespace(1), Alphanumeric("hello"), Symbol('('), Symbol(')'), Symbol(':'), Newline,
                    Whitespace(4), Alphanumeric("println"), Symbol('('), StringValue("Hello, world"), Symbol(')'), Newline
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