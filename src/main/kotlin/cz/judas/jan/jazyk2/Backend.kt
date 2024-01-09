package cz.judas.jan.jazyk2

import cz.judas.jan.jazyk2.ast.typed.Expression
import cz.judas.jan.jazyk2.ast.typed.FullyQualifiedType
import cz.judas.jan.jazyk2.ast.typed.Function
import cz.judas.jan.jazyk2.ast.typed.Package
import cz.judas.jan.jazyk2.ast.typed.Statement
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.writeLines
import kotlin.io.path.writeText

interface Backend {
    fun compile(source: Package, buildDir: Path, executableName: String)
}

class GoBackend : Backend {
    override fun compile(source: Package, buildDir: Path, executableName: String) {

        val moduleFile = buildDir / "go.mod"
        moduleFile.writeLines(
            listOf(
                "module jazyk/main",  // TODO configurable name
                "go 1.20"
            )
        )

        val goSourceCode = StringBuilder("package main").append("\n")

        val mainFunction = source.functions.first { it.annotations.any { it.type == entrypoint } }
        goSourceCode.append(
            """
            func main() {
                ${mainFunction.name.asIdentifier()}()
            }
            
        """.trimIndent()
        )

        source.functions.forEach { goSourceCode.append(UserDefinedFunction(it).generateCode()) }
        stdlib.values.forEach { goSourceCode.append(it.generateCode()) }

        val goFile = buildDir / "main.go"
        goFile.writeText(goSourceCode.toString())
        val process = ProcessBuilder(listOf("go", "build", "-ldflags", "-s -w")).directory(buildDir.toFile()).start()
        process.waitFor()
    }

    companion object {
        val entrypoint = FullyQualifiedType(listOf("stdlib", "entrypoint"))

        private val println = NativeFunction(
            """
            func stdlib_io_println(arg string) {
                println(arg)                            
            }
            """.trimIndent()
        )

        val stdlib = mapOf(
            FullyQualifiedType(listOf("stdlib", "io", "println")) to println
        )
    }

    interface GoFunction {
        fun generateCode(): String
    }

    class NativeFunction(private val code: String) : GoFunction {
        override fun generateCode(): String {
            return code
        }
    }

    class UserDefinedFunction(private val function: Function) : GoFunction {
        override fun generateCode(): String {
            val goSourceCode = StringBuilder("func ").append(function.name.asIdentifier()).append("() {\n")
            function.body.forEach { statement ->
                goSourceCode.append("\t")
                goSourceCode.append(
                    when (statement) {
                        is Statement.FunctionCallStatement ->
                            statement.functionCall.function.asIdentifier() + "(" + statement.functionCall.arguments.joinToString(", ", transform = ::generateExpressionCode) + ")\n"
                    }
                )
            }
            goSourceCode.append("}\n")
            return goSourceCode.toString()
        }

        private fun generateExpressionCode(expression: Expression): String {
            return when (expression) {
                is Expression.StringConstant -> "\"${expression.value}\"" // TODO escaping
            }
        }
    }
}

private fun FullyQualifiedType.asIdentifier() = path.joinToString("_")
