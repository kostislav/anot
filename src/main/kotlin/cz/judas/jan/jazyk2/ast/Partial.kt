package cz.judas.jan.jazyk2.ast

import cz.judas.jan.jazyk2.SymbolMap
import cz.judas.jan.jazyk2.ast.typed.FullyQualifiedType
import cz.judas.jan.jazyk2.ast.untyped.Annotation
import cz.judas.jan.jazyk2.ast.untyped.Expression

data class PartiallyTypedSourceFile(
    val filePackage: List<String>,
    val imports: Map<String, FullyQualifiedType>,
    val functions: List<PartiallyTypedFunction>,
) {
    fun symbolMap(): SymbolMap {
        return SymbolMap(
            functions
                .map { it.signature }
                .associateBy{ FullyQualifiedType(filePackage + it.name) }
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
    val returnType: FullyQualifiedType,
)