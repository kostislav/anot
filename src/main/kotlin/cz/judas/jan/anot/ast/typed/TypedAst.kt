package cz.judas.jan.anot.ast.typed

import cz.judas.jan.anot.Stdlib
import cz.judas.jan.anot.ast.FunctionParameter

data class Package(
    val name: FullyQualifiedType,
    private val functions: List<Function>,
) {
    operator fun plus(other: Package): Package {
        if (this.name == other.name) {
            return Package(name, functions + other.functions)
        } else {
            throw RuntimeException("Cannot combine package fragments for different packages")
        }
    }

    fun functions(): Map<FullyQualifiedType, Function> {
        return functions.associateBy { name.child(it.name) }
    }
}

data class Function(
    val annotations: List<Annotation>,
    val name: String,
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

    data class MethodCall(val receiver: VariableReference, val methodName: String, val arguments: List<Expression>, val returnType: FullyQualifiedType): Expression {
        override fun resultType(): FullyQualifiedType = returnType
    }

    data class VariableReference(val name: String, val type: FullyQualifiedType): Expression {
        override fun resultType(): FullyQualifiedType = type
    }
}

@JvmInline
value class FullyQualifiedType(val path: List<String>) {
    fun name(): String = path.last()

    fun child(name: String): FullyQualifiedType {
        return FullyQualifiedType(path + name)
    }

    fun child(path: List<String>): FullyQualifiedType {
        return FullyQualifiedType(path + path)
    }
}