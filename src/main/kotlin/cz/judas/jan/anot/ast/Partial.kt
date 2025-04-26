package cz.judas.jan.anot.ast

import cz.judas.jan.anot.SymbolMap
import cz.judas.jan.anot.ast.typed.FullyQualifiedName
import cz.judas.jan.anot.ast.untyped.Annotation
import cz.judas.jan.anot.ast.untyped.Expression

data class PartiallyTypedSourceFile(
    val filePackage: FullyQualifiedName,
    val topLevelSymbols: Map<String, FullyQualifiedName>,
    val functions: List<PartiallyTypedFunction>,
) {
    fun symbolMap(): SymbolMap {
        return SymbolMap(
            functions
                .map { it.signature }
                .associateBy { filePackage.child(it.name) },
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
    val returnType: FullyQualifiedName,
) {
    fun renamedTo(newName: String): FunctionSignature {
        return copy(name = newName)
    }
}


data class ClassSignature(
    val name: String,
    val methods: Map<String, FunctionSignature>,
)

data class FunctionParameter(val name: String, val type: FullyQualifiedName)
