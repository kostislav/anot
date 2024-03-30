package cz.judas.jan.jazyk2.ast.typed

data class Package(
    val functions: List<Function>
)

data class Function(
    val annotations: List<Annotation>,
    val name: FullyQualifiedType,
    val body: List<Expression>
)

data class Annotation(val type: FullyQualifiedType)

sealed interface Expression {
    data class StringConstant(val value: String) : Expression
    data class FunctionCall(val function: FullyQualifiedType, val arguments: List<Expression>): Expression
}

@JvmInline
value class FullyQualifiedType(val path: List<String>) {
    fun name(): String = path.last()
}