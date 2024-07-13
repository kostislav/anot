package cz.judas.jan.jazyk2

import cz.judas.jan.jazyk2.ast.ClassSignature
import cz.judas.jan.jazyk2.ast.FunctionSignature
import cz.judas.jan.jazyk2.ast.typed.FullyQualifiedType

// TODO use Entry?
data class SymbolMap(
    val functions: Map<FullyQualifiedType, FunctionSignature>,
    val classes: Map<FullyQualifiedType, ClassSignature>,
) {
    operator fun plus(other: SymbolMap): SymbolMap {
        return SymbolMap(
            functions + other.functions,
            classes + other.classes,
        ) // TODO fail on conflict
    }
}