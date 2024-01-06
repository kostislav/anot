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
import java.io.Reader
import kotlin.test.assertEquals

class LexerTest {
    private lateinit var lexer: Lexer

    @BeforeEach
    fun setUp() {
        lexer = Lexer()
    }

    @Test
    fun parsesHelloWorld() {
        val input = stream(
            """
            import /stdlib/io/println
            import /stdlib/entrypoint

            @entrypoint
            def main():
                println('Hello, world')
        """.trimIndent()
        )

        val parsedLines = lexer.parseTokens(input)

        assertThat(
            parsedLines, equalTo(
                listOf(
                    Alphanumeric("import"), Whitespace(1), Symbol("/"), Alphanumeric("stdlib"), Symbol("/"), Alphanumeric("io"), Symbol("/"), Alphanumeric("println"), Newline,
                    Alphanumeric("import"), Whitespace(1), Symbol("/"), Alphanumeric("stdlib"), Symbol("/"), Alphanumeric("entrypoint"), Newline,
                    EmptyLine,
                    Symbol("@"), Alphanumeric("entrypoint"), Newline,
                    Alphanumeric("def"), Whitespace(1), Alphanumeric("main"), Symbol("():"), Newline,
                    Whitespace(4), Alphanumeric("println"), Symbol("("), StringValue("Hello, world"), Symbol(")"), Newline
                )
            )
        )
    }

    @Test
    fun failsForInvalidInput() {
        val input = stream("import ^eh")

        val exception = assertThrows<RuntimeException> { lexer.parseTokens(input) }
        assertEquals(exception.message, "Unexpected character ^ on line 1 at position 8")
    }

    private fun stream(content: String): Reader {
        return content.reader()
    }
}