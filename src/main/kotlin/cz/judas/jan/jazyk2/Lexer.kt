package cz.judas.jan.jazyk2

import java.io.Reader

class Lexer {
    private val symbols = setOf('/', '(', ')', '@', ':', '{', '}', '-', '>')

    fun parseTokens(input: Reader): List<Token> {
        return input.readLines()
            .flatMapIndexed(::parseLine)
    }

    private fun parseLine(lineNumber: Int, line: String): List<Token> {
        if (line.isEmpty()) {
            return listOf(Token.EmptyLine)
        } else {
            val tokens = mutableListOf<Token>()
            var pos = 0

            while (pos < line.length) {
                if (isSymbol(line[pos])) {
                    while (pos < line.length && isSymbol(line[pos])) {
                        tokens += Token.Symbol(line[pos])
                        pos++
                    }
                } else if (line[pos] == ' ') {
                    val start = pos
                    while (pos < line.length && line[pos] == ' ') {
                        pos++
                    }
                    tokens += Token.Whitespace(pos - start)
                } else if (isAlphanumeric(line[pos])) {
                    val start = pos
                    while (pos < line.length && isAlphanumeric(line[pos])) {
                        pos++
                    }
                    tokens += Token.Alphanumeric(line.substring(start, pos))
                } else if (line[pos] == '\'') {
                    pos++
                    val start = pos
                    // TODO escape sequences
                    while (line[pos] != '\'') {
                        pos++
                        if (pos == line.length) {
                            throw RuntimeException("Unterminated string literal on line ${lineNumber + 1} starting at position ${start}")
                        }
                    }
                    tokens += Token.StringValue(line.substring(start, pos))
                    pos++
                } else {
                    throw RuntimeException("Unexpected character ${line[pos]} on line ${lineNumber + 1} at position ${pos + 1}")
                }
            }

            tokens += Token.Newline

            return tokens
        }
    }

    private fun isAlphanumeric(c: Char): Boolean {
        return c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9'
    }

    private fun isSymbol(c: Char): Boolean {
        return c in symbols
    }
}

sealed interface Token {
    data class Whitespace(val amount: Int) : Token
    data object Newline: Token
    data object EmptyLine: Token
    data class Alphanumeric(val value: String): Token
    data class Symbol(val value: Char): Token
    data class StringValue(val value: String): Token
}