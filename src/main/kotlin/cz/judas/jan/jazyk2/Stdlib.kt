package cz.judas.jan.jazyk2

import cz.judas.jan.jazyk2.ast.typed.FullyQualifiedType

object Stdlib {
    val entrypoint = FullyQualifiedType(listOf("stdlib", "entrypoint"))

    val println = FullyQualifiedType(listOf("stdlib", "io", "println"))
}