package cz.judas.jan.anot

import cz.judas.jan.anot.ast.ClassSignature
import cz.judas.jan.anot.ast.FunctionSignature
import cz.judas.jan.anot.ast.typed.FullyQualifiedName

// TODO use Entry?
data class SymbolMap(
    val functions: Map<FullyQualifiedName, FunctionSignature>,
    val classes: Map<FullyQualifiedName, ClassSignature>,
) {
    operator fun plus(other: SymbolMap): SymbolMap {
        return SymbolMap(
            functions + other.functions,
            classes + other.classes,
        ) // TODO fail on conflict
    }
}