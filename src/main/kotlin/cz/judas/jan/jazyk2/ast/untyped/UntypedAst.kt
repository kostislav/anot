package cz.judas.jan.jazyk2.ast.untyped

data class SourceFile(
    val imports: List<ImportStatement>,
    val definitions: List<TopLevelDefinition>
)

data class ImportStatement(val importedPath: List<String>)

sealed interface TopLevelDefinition {
    data class Function(
        val annotations: List<Annotation>,
        val name: String,
        val body: List<Statement>
    ) : TopLevelDefinition
}

data class Annotation(val type: String)

sealed interface Statement {
    data class FunctionCallStatement(val functionCall: FunctionCall) : Statement
}

sealed interface Expression {
    data class StringConstant(val value: String) : Expression
}

data class FunctionCall(val functionName: String, val arguments: List<Expression>)