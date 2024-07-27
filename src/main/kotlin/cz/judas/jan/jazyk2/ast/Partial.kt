package cz.judas.jan.jazyk2.ast

import cz.judas.jan.jazyk2.SymbolMap
import cz.judas.jan.jazyk2.ast.typed.FullyQualifiedType
import cz.judas.jan.jazyk2.ast.untyped.Annotation
import cz.judas.jan.jazyk2.ast.untyped.Expression

data class PartiallyTypedSourceFile(
    val filePackage: List<String>,
    val imports: Map<String, FullyQualifiedType>,  // TODO rename to topLevelSymbols
    val functions: List<PartiallyTypedFunction>,
) {
    fun symbolMap(): SymbolMap {
        return SymbolMap(
            functions
                .map { it.signature }
                .associateBy { FullyQualifiedType(filePackage + it.name) },
            emptyMap(),
        )
    }
}

data class PartiallyTypedFunction(
    val annotations: List<Annotation>,
    val signature: FunctionSignature,
    val body: List<Expression>,
)

data class FunctionSignature(
    val name: String,
    val parameters: List<FunctionParameter>,
    val returnType: FullyQualifiedType,
)


data class ClassSignature(
    val name: String,
    val methods: Map<String, FunctionSignature>,
)

data class FunctionParameter(val name: String, val type: FullyQualifiedType)
