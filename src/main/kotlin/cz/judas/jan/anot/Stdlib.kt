package cz.judas.jan.anot

import cz.judas.jan.anot.ast.ClassSignature
import cz.judas.jan.anot.ast.FunctionParameter
import cz.judas.jan.anot.ast.FunctionSignature
import cz.judas.jan.anot.ast.typed.FullyQualifiedType

object Stdlib {
    val entrypoint = FullyQualifiedType(listOf("stdlib", "entrypoint"))

    val void = FullyQualifiedType(listOf("stdlib", "Void"))

    val string = FullyQualifiedType(listOf("stdlib", "String"))

    val stdio = FullyQualifiedType(listOf("stdlib", "io", "Stdio"))

    fun symbolMap(): SymbolMap {
        return SymbolMap(
            emptyMap(),
            mapOf(
                void to ClassSignature("Void", emptyMap()),
                string to ClassSignature("String", emptyMap()),
                stdio to ClassSignature("Stdio", mapOf(
                    "println" to FunctionSignature("println", listOf(FunctionParameter("value", string)), void),
                ))
            )
        )
    }
}
