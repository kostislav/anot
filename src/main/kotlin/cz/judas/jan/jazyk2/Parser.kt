package cz.judas.jan.jazyk2

import cz.judas.jan.jazyk2.ast.untyped.Annotation
import cz.judas.jan.jazyk2.ast.untyped.Expression
import cz.judas.jan.jazyk2.ast.untyped.FunctionCall
import cz.judas.jan.jazyk2.ast.untyped.ImportStatement
import cz.judas.jan.jazyk2.ast.untyped.SourceFile
import cz.judas.jan.jazyk2.ast.untyped.Statement
import cz.judas.jan.jazyk2.ast.untyped.TopLevelDefinition
import kotlin.reflect.KClass
import kotlin.reflect.cast

class Parser {
    fun parseFile(tokens: List<Token>): SourceFile {
        val stream = TokenStream(tokens)

        return SourceFile(
            imports(stream),
            topLevelDefinitions(stream)
        )
    }

    private fun imports(stream: TokenStream): List<ImportStatement> {
        val imports = mutableListOf<ImportStatement>()

        while (stream.hasNext() && stream.current() == importKeyword) {
            stream.advance()
            stream.expectType(Token.Whitespace::class)
            imports += ImportStatement(
                stream.consumeUntil { it is Token.Newline }
                    .filterIsInstance<Token.Alphanumeric>()
                    .map { it.value }
            )
            stream.advance()
        }

        stream.expect(Token.EmptyLine)
        stream.dropWhile { it == Token.EmptyLine }

        return imports
    }

    private fun topLevelDefinitions(stream: TokenStream): List<TopLevelDefinition> {
        val definitions = mutableListOf<TopLevelDefinition>()

        while (stream.hasNext()) {
            definitions += topLevelDefinition(stream)
        }

        return definitions
    }

    private fun topLevelDefinition(stream: TokenStream): TopLevelDefinition {
        val annotations = annotations(stream)

        if (stream.current() == defKeyword) {
            stream.advance()
            stream.expectType(Token.Whitespace::class)
            val name = stream.expectType(Token.Alphanumeric::class).value
            stream.expect(Token.Symbol("():"))
            stream.expect(Token.Newline)
            val indent = stream.expectType(Token.Whitespace::class).amount
            return TopLevelDefinition.Function(
                annotations,
                name,
                statements(stream, indent)
            )
        } else {
            throw RuntimeException("Just functions for now")
        }
    }

    private fun annotations(stream: TokenStream): List<Annotation> {
        val annotations = mutableListOf<Annotation>()
        while (stream.current() == atSign && stream.peekNext() is Token.Alphanumeric) {
            stream.advance()
            annotations += Annotation((stream.current() as Token.Alphanumeric).value)
            stream.advance()
            if (stream.current() is Token.Whitespace || stream.current() == Token.Newline) {
                stream.advance()
            }
        }
        return annotations
    }

    private fun statements(stream: TokenStream, indent: Int): List<Statement> {
        val firstToken = stream.current()
        if (firstToken is Token.Alphanumeric && stream.peekNext() == openingRoundBracket) {
            stream.skip(2)
            val functionName = firstToken.value
            val parameters = listOf(expression(stream))
            stream.expect(closingRoundBracket)
            stream.expect(Token.Newline)
            return listOf(Statement.FunctionCallStatement(FunctionCall(functionName, parameters)))
        } else {
            throw RuntimeException("Just function calls for now")
        }
    }

    private fun expression(stream: TokenStream): Expression {
        val firstToken = stream.current()
        if (firstToken is Token.StringValue) {
            stream.advance()
            return Expression.StringConstant(firstToken.value)
        } else {
            throw RuntimeException("Just strings for now")
        }
    }

    companion object {
        val importKeyword = Token.Alphanumeric("import")
        val defKeyword = Token.Alphanumeric("def")
        val atSign = Token.Symbol("@")
        val openingRoundBracket = Token.Symbol("(")
        val closingRoundBracket = Token.Symbol(")")
    }

    private class TokenStream(private val tokens: List<Token>) {
        private var pos = 0

        fun current(): Token = tokens[pos]

        fun peekNext(): Token = tokens[pos + 1]

        fun skip(howMany: Int) {
            pos += howMany
        }

        fun hasNext(): Boolean = pos < tokens.size

        fun advance() {
            pos++
        }

        fun consumeUntil(predicate: (Token) -> Boolean): List<Token> {
            val chunk = tokens.subList(pos, tokens.size).takeWhile { !predicate(it) }
            pos += chunk.size
            return chunk
        }

        fun dropWhile(predicate: (Token) -> Boolean) {
            while (pos < tokens.size && predicate(tokens[pos])) {
                pos++
            }
        }

//        TODO do we need this?
        fun expect(token: Token) {
            if (current() != token) {
                throw RuntimeException("Expected ${token}, got ${current()}")
            } else {
                advance()
            }
        }

        fun <T: Token> expectType(tokenType: KClass<T>): T {
            val currentToken = current()
            if (!tokenType.isInstance(currentToken)) {
                throw RuntimeException("Expected ${tokenType}, got $currentToken")
            } else {
                advance()
                return tokenType.cast(currentToken)
            }
        }
    }
}