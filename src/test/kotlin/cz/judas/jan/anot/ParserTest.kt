package cz.judas.jan.anot

import cz.judas.jan.anot.ast.untyped.Expression
import cz.judas.jan.anot.ast.untyped.ImportStatement
import cz.judas.jan.anot.ast.untyped.SourceFile
import cz.judas.jan.anot.ast.untyped.TopLevelDefinition
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.io.path.reader

class ParserTest {
    @Nested
    inner class LexerParserIntegrationTest {
        @Test
        fun parsesHelloWorld() {
            val lexer = Lexer()
            val parser = Parser()
            val input = Path("examples/hello/src/hello.jaz").reader()

            val tokens = lexer.parseTokens(input)
            val ast = parser.parseFile(tokens)

            assertThat(
                ast,
                equalTo(
                    SourceFile(
                        listOf(
                            ImportStatement(listOf("stdlib", "io", "Stdio"), isAbsolute = true),
                        ),
                        listOf(
                            TopLevelDefinition.Function(
                                emptyList(),
                                "hello",
                                listOf(
                                    TopLevelDefinition.Function.Parameter("stdio", "Stdio"),
                                ),
                                null,
                                listOf(
                                    Expression.MethodCall(
                                        "stdio",
                                        "println",
                                        listOf(Expression.StringConstant("Hello, world"))
                                    )
                                )
                            )
                        )
                    )
                )
            )
        }
    }
}