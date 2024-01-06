package cz.judas.jan.jazyk2

import cz.judas.jan.jazyk2.ast.untyped.Annotation
import cz.judas.jan.jazyk2.ast.untyped.Expression
import cz.judas.jan.jazyk2.ast.untyped.FunctionCall
import cz.judas.jan.jazyk2.ast.untyped.ImportStatement
import cz.judas.jan.jazyk2.ast.untyped.SourceFile
import cz.judas.jan.jazyk2.ast.untyped.Statement
import cz.judas.jan.jazyk2.ast.untyped.TopLevelDefinition
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ParserTest {
    @Nested
    inner class LexerParserIntegrationTest {
        @Test
        fun parsesHelloWorld() {
            val lexer = Lexer()
            val parser = Parser()
            val input = """
            import /stdlib/io/println

            @entrypoint
            def main():
                println('Hello, world')
        """.trimIndent().reader()

            val tokens = lexer.parseTokens(input)
            val ast = parser.parseFile(tokens)

            assertThat(
                ast,
                equalTo(
                    SourceFile(
                        listOf(ImportStatement(listOf("stdlib", "io", "println"))),
                        listOf(
                            TopLevelDefinition.Function(
                                listOf(Annotation("entrypoint")),
                                "main",
                                listOf(
                                    Statement.FunctionCallStatement(
                                        FunctionCall(
                                            "println",
                                            listOf(Expression.StringConstant("Hello, world"))
                                        )
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