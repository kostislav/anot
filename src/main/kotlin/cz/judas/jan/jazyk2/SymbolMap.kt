package cz.judas.jan.jazyk2

import cz.judas.jan.jazyk2.ast.FunctionSignature
import cz.judas.jan.jazyk2.ast.typed.FullyQualifiedType

data class SymbolMap(
    val functions: Map<FullyQualifiedType, FunctionSignature>,
) {
    operator fun plus(other: SymbolMap): SymbolMap {
        return SymbolMap(functions + other.functions) // TODO fail on conflict
    }
}