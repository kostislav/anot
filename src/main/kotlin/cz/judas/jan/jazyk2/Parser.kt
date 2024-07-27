package cz.judas.jan.jazyk2

import cz.judas.jan.jazyk2.ast.untyped.Annotation
import cz.judas.jan.jazyk2.ast.untyped.Expression
import cz.judas.jan.jazyk2.ast.untyped.ImportStatement
import cz.judas.jan.jazyk2.ast.untyped.SourceFile
import cz.judas.jan.jazyk2.ast.untyped.TopLevelDefinition
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.typeOf

class Parser {
    fun parseFile(tokens: List<Token>): SourceFile {
        val stream = TokenStream(tokens)

        return SourceFile(
            imports(stream),
            topLevelDefinitions(stream)
        )
    }

    private fun imports(stream: TokenStream): List<ImportStatement> {
        return if (stream.current() == importKeyword) {
            stream.advance()
            val imports = mutableListOf<ImportStatement>()
            if (stream.current() == colon) {
                stream.advance()
                stream.expect(Token.Newline)
                val indent = stream.expectType<Token.Whitespace>()
                imports += importStatement(stream, Token.Newline)
                while (stream.current() != Token.EmptyLine) {
                    stream.expect(indent)
                    imports += importStatement(stream, Token.Newline)
                }
            } else {
                stream.expectType<Token.Whitespace>()
                stream.expect(openingCurlyBracket)
                stream.expectType<Token.Whitespace>()
                imports += importStatement(stream, closingCurlyBracket)
                stream.expect(Token.Newline)
            }
            stream.advance()
            imports
        } else {
            emptyList()
        }
    }

    private fun importStatement(stream: TokenStream, delimiter: Token): ImportStatement {
        val tokens = stream.consumeUntil { it == delimiter }
        val result = ImportStatement(
            tokens
                .filterIsInstance<Token.Identifier>()
                .map { it.value },
            isAbsolute = tokens.first() == slash,
        )
        stream.advance()
        return result
    }

    private fun topLevelDefinitions(stream: TokenStream): List<TopLevelDefinition> {
        val definitions = mutableListOf<TopLevelDefinition>()

        while (stream.hasNext()) {
            definitions += topLevelDefinition(stream)
            stream.dropWhile { it == Token.EmptyLine }
        }

        return definitions
    }

    private fun topLevelDefinition(stream: TokenStream): TopLevelDefinition {
        val annotations = annotations(stream)

        if (stream.current() == defKeyword) {
            stream.advance()
            stream.expectType<Token.Whitespace>()
            val functionName = stream.expectType<Token.Identifier>().value
            stream.expect(openingRoundBracket)
            val parameters = mutableListOf<TopLevelDefinition.Function.Parameter>()
            while (stream.current() != closingRoundBracket) {
                val parameterName = stream.expectType<Token.Identifier>().value
                stream.expect(colon)
                stream.expectType<Token.Whitespace>()
                val parameterType = stream.expectType<Token.Identifier>().value
                parameters += TopLevelDefinition.Function.Parameter(parameterName, parameterType)
                if (stream.current() == comma) {
                    stream.advance()
                    stream.expectType<Token.Whitespace>()
                }
            }
            stream.expect(closingRoundBracket)
            val returnType = if (stream.current() is Token.Whitespace) {
                stream.advance()
                stream.expect(Token.Symbol('-'))
                stream.expect(Token.Symbol('>'))
                stream.expectType(Token.Whitespace::class)
                stream.expectType<Token.Identifier>().value
            } else {
                null
            }
            stream.expect(colon)
            stream.expect(Token.Newline)
            return TopLevelDefinition.Function(
                annotations,
                functionName,
                parameters,
                returnType,
                body(stream, 0)
            )
        } else {
            throw RuntimeException("Just functions for now")
        }
    }

    private fun annotations(stream: TokenStream): List<Annotation> {
        val annotations = mutableListOf<Annotation>()
        while (stream.current() == atSign && stream.peekNext() is Token.Identifier) {
            stream.advance()
            annotations += Annotation((stream.current() as Token.Identifier).value)
            stream.advance()
            if (stream.current() is Token.Whitespace || stream.current() == Token.Newline) {
                stream.advance()
            }
        }
        return annotations
    }

    private fun body(stream: TokenStream, previousIndent: Int): List<Expression> {
        val indent = stream.expectType<Token.Whitespace>().amount
        if (indent <= previousIndent) {
            throw RuntimeException("Indentation problem")
        }
        val result = mutableListOf(
            expression(stream)
        )
        stream.expect(Token.Newline)
        while (stream.hasNext()) {
            val current = stream.current()
            if (current is Token.Whitespace && current.amount == indent) {
                stream.advance()
                result += expression(stream)
                stream.expect(Token.Newline)
            } else {
                break
            }
        }
        return result
    }

    private fun expression(stream: TokenStream): Expression {
        val firstToken = stream.current()
        return if (firstToken is Token.StringValue) {
            stream.advance()
            Expression.StringConstant(firstToken.value)
        } else if (firstToken is Token.Identifier) {
            if (stream.peekNext() == openingRoundBracket) {
                stream.skip(2)
                val functionName = firstToken.value
                val parameters = functionParameters(stream)
                stream.expect(closingRoundBracket)
                Expression.FunctionCall(functionName, parameters)
            } else if (stream.peekNext() == period) {
                val receiver = firstToken.value
                stream.skip(2)
                val methodName = stream.expectType<Token.Identifier>().value
                stream.expect(openingRoundBracket)
                val parameters = functionParameters(stream)
                stream.expect(closingRoundBracket)
                Expression.MethodCall(receiver, methodName, parameters)
            } else {
                val name = firstToken.value
                stream.advance()
                Expression.VariableReference(name)
            }
        } else {
            throw RuntimeException("Just strings for now")
        }
    }

    private fun functionParameters(stream: TokenStream): MutableList<Expression> {
        val parameters = mutableListOf<Expression>()
        while (stream.current() != closingRoundBracket) {
            parameters += expression(stream)
            if (stream.current() == comma) {
                stream.advance()
                stream.expectType<Token.Whitespace>()
            }
        }
        return parameters
    }

    companion object {
        val importKeyword = Token.Identifier("import")
        val defKeyword = Token.Identifier("def")
        val atSign = Token.Symbol('@')
        val openingRoundBracket = Token.Symbol('(')
        val closingRoundBracket = Token.Symbol(')')
        val openingCurlyBracket = Token.Symbol('{')
        val closingCurlyBracket = Token.Symbol('}')
        val colon = Token.Symbol(':')
        val slash = Token.Symbol('/')
        val comma = Token.Symbol(',')
        val period = Token.Symbol('.')
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

        @Suppress("UNCHECKED_CAST")
        inline fun <reified T: Token> expectType(): T {
            return expectType(typeOf<T>().classifier as KClass<T>)
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