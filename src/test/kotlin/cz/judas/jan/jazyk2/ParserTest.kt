package cz.judas.jan.jazyk2

import cz.judas.jan.jazyk2.ast.untyped.Annotation
import cz.judas.jan.jazyk2.ast.untyped.Expression
import cz.judas.jan.jazyk2.ast.untyped.ImportStatement
import cz.judas.jan.jazyk2.ast.untyped.SourceFile
import cz.judas.jan.jazyk2.ast.untyped.TopLevelDefinition
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
                            ImportStatement(listOf("stdlib", "io", "println")),
                            ImportStatement(listOf("stdlib", "entrypoint"))
                        ),
                        listOf(
                            TopLevelDefinition.Function(
                                listOf(Annotation("entrypoint")),
                                "hello",
                                listOf(
                                    Expression.FunctionCall(
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