package cz.judas.jan.jazyk2.ast.typed

import cz.judas.jan.jazyk2.Stdlib

data class Package(
    val functions: List<Function>
)

data class Function(
    val annotations: List<Annotation>,
    val name: FullyQualifiedType,
    val returnType: FullyQualifiedType,
    val body: List<Expression>,
)

data class Annotation(val type: FullyQualifiedType)


sealed interface Expression {
    fun resultType(): FullyQualifiedType

    data class StringConstant(val value: String) : Expression {
        override fun resultType(): FullyQualifiedType = Stdlib.string
    }

    data class FunctionCall(val function: FullyQualifiedType, val arguments: List<Expression>, val returnType: FullyQualifiedType): Expression {
        override fun resultType(): FullyQualifiedType = returnType
    }
}

@JvmInline
value class FullyQualifiedType(val path: List<String>) {
    fun name(): String = path.last()
}