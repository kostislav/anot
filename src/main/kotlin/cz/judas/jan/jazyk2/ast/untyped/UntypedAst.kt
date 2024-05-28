package cz.judas.jan.jazyk2.ast.untyped

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
        val returnType: String?,
        val body: List<Expression>
    ) : TopLevelDefinition
}

data class Annotation(val type: String)

sealed interface Expression {
    data class StringConstant(val value: String) : Expression
    data class FunctionCall(val functionName: String, val arguments: List<Expression>): Expression
}
