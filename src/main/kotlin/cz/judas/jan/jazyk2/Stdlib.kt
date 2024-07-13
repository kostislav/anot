package cz.judas.jan.jazyk2

import cz.judas.jan.jazyk2.ast.ClassSignature
import cz.judas.jan.jazyk2.ast.FunctionSignature
import cz.judas.jan.jazyk2.ast.typed.FullyQualifiedType

object Stdlib {
    val entrypoint = FullyQualifiedType(listOf("stdlib", "entrypoint"))

    val void = FullyQualifiedType(listOf("stdlib", "Void"))

    val string = FullyQualifiedType(listOf("stdlib", "String"))

    val println = FullyQualifiedType(listOf("stdlib", "io", "println"))

    fun symbolMap(): SymbolMap {
        return SymbolMap(
            mapOf(
                println to FunctionSignature("println", void)
            ),
            mapOf(
                void to ClassSignature("Void"),
                string to ClassSignature("String"),
            )
        )
    }
}
