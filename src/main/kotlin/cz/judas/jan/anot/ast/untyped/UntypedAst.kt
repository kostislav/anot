package cz.judas.jan.anot.ast.untyped

data class SourceFile(
    val imports: List<ImportStatement>,
    val definitions: List<TopLevelDefinition>
)

data class ImportStatement(val importedPath: List<String>, val isAbsolute: Boolean)

sealed interface TopLevelDefinition {
    val name: String

    data class Function(
        val annotations: List<Annotation>,
        override val name: String,
        val parameters: List<Parameter>,
        val returnType: String?,
        val body: List<Expression>
    ) : TopLevelDefinition {
        data class Parameter(val name: String, val type: String)
    }
}

data class Annotation(val type: String)

sealed interface Expression {
    data class StringConstant(val value: String) : Expression
    data class FunctionCall(val functionName: String, val arguments: List<Expression>): Expression
    data class MethodCall(val receiver: String, val methodName: String, val arguments: List<Expression>): Expression
    data class VariableReference(val name: String): Expression
}
