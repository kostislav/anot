package cz.judas.jan.jazyk2.ast.typed

data class Package(
    val functions: List<Function>
)

data class Function(
    val annotations: List<Annotation>,
    val name: String,
    val body: List<Statement>
)

data class Annotation(val type: FullyQualifiedType)

sealed interface Statement {
    data class FunctionCallStatement(val functionCall: FunctionCall) : Statement
}

sealed interface Expression {
    data class StringConstant(val value: String) : Expression
}

data class FunctionCall(val function: FullyQualifiedType, val arguments: List<Expression>)

@JvmInline
value class FullyQualifiedType(val path: List<String>)