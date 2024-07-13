package cz.judas.jan.jazyk2.ast.typed

import cz.judas.jan.jazyk2.Stdlib
import cz.judas.jan.jazyk2.ast.FunctionParameter

data class Package(
    val functions: List<Function>
) {
    operator fun plus(other: Package): Package {
        return Package(functions + other.functions)
    }
}

data class Function(
    val annotations: List<Annotation>,
    val name: FullyQualifiedType,
    val parameters: List<FunctionParameter>,
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

    data class VariableReference(val name: String, val type: FullyQualifiedType): Expression {
        override fun resultType(): FullyQualifiedType = type
    }
}

@JvmInline
value class FullyQualifiedType(val path: List<String>) {
    fun name(): String = path.last()
}