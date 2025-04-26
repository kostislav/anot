package cz.judas.jan.anot.ast.typed

import cz.judas.jan.anot.Stdlib
import cz.judas.jan.anot.ast.FunctionSignature

data class Package(
    val name: FullyQualifiedName,
    private val functions: List<Function>,
) {
    operator fun plus(other: Package): Package {
        if (this.name == other.name) {
            return Package(name, functions + other.functions)
        } else {
            throw RuntimeException("Cannot combine package fragments for different packages")
        }
    }

    fun functions(): Map<FullyQualifiedName, Function> {
        return functions.associateBy { name.child(it.signature.name) }
    }
}

data class Function(
    val annotations: List<Annotation>,
    val signature: FunctionSignature,
    val body: List<Expression>,
)

data class Annotation(val type: FullyQualifiedName)


sealed interface Expression {
    fun resultType(): FullyQualifiedName

    data class StringConstant(val value: String) : Expression {
        override fun resultType(): FullyQualifiedName = Stdlib.string
    }

    data class FunctionCall(val function: FullyQualifiedName, val arguments: List<Expression>, val returnType: FullyQualifiedName): Expression {
        override fun resultType(): FullyQualifiedName = returnType
    }

    data class MethodCall(val receiver: VariableReference, val methodName: String, val arguments: List<Expression>, val returnType: FullyQualifiedName): Expression {
        override fun resultType(): FullyQualifiedName = returnType
    }

    data class VariableReference(val name: String, val type: FullyQualifiedName): Expression {
        override fun resultType(): FullyQualifiedName = type
    }
}

@JvmInline
value class FullyQualifiedName(val path: List<String>) {
    fun name(): String = path.last()

    fun child(name: String): FullyQualifiedName {
        return FullyQualifiedName(path + name)
    }

    fun child(path: List<String>): FullyQualifiedName {
        return FullyQualifiedName(path + path)
    }
}