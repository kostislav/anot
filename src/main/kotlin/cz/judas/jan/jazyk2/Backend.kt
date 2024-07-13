package cz.judas.jan.jazyk2

import cz.judas.jan.jazyk2.ast.typed.Expression
import cz.judas.jan.jazyk2.ast.typed.FullyQualifiedType
import cz.judas.jan.jazyk2.ast.typed.Function
import cz.judas.jan.jazyk2.ast.typed.Package
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.writeLines
import kotlin.io.path.writeText

interface Backend {
    fun compile(source: Package, buildDir: Path, mainFunction: FullyQualifiedType, executableName: String)
}

class GoBackend : Backend {
    override fun compile(source: Package, buildDir: Path, mainFunction: FullyQualifiedType, executableName: String) {

        val moduleFile = buildDir / "go.mod"
        moduleFile.writeLines(
            listOf(
                "module jazyk/${executableName}",
                "go 1.20"
            )
        )

        val goSourceCode = StringBuilder("package main").append("\n\n")
        goSourceCode.append("import ( \"os\" )\n")

        goSourceCode.append(
            """
                
            func main() {
                ${mainFunction.asIdentifier()}()
            }
            
            
        """.trimIndent()
        )

        source.functions.forEach { goSourceCode.append(UserDefinedFunction(it).generateCode()).append("\n") }
        stdlibGoImpl.values.forEach { goSourceCode.append(it.generateCode()).append("\n") }

        val goFile = buildDir / "${executableName}.go"
        goFile.writeText(goSourceCode.toString())
        val process = ProcessBuilder(listOf("go", "build", "-ldflags", "-s -w")).directory(buildDir.toFile()).start()
        process.waitFor()
        if (process.exitValue() != 0) {
            throw RuntimeException("Backend failed: " + process.errorStream.reader().readText())
        }
    }

    companion object {
        private val println = NativeFunction(
            """
            func stdlib_io_println(arg string) {
                os.Stdout.WriteString(arg)                     
                os.Stdout.WriteString("\n")                     
            }
            """.trimIndent()
        )

        val stdlibGoImpl = mapOf(
            Stdlib.println to println
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
            val goSourceCode = StringBuilder("func ").append(function.name.asIdentifier()).append("(")
            goSourceCode.append(function.parameters.map { "${it.name} ${nonVoidType(it.type)}" }.joinToString(","))
            goSourceCode.append(") ")
            returnType(function.returnType)?.let { goSourceCode.append(it).append(" ") }
            goSourceCode.append("{\n")
            val bodyIterator = function.body.iterator()
            while (bodyIterator.hasNext()) {
                val expression = bodyIterator.next()
                goSourceCode.append("\t")
                if (function.returnType != Stdlib.void && !bodyIterator.hasNext()) {
                    goSourceCode.append("return ")
                }
                goSourceCode.append(generateExpressionCode(expression)).append("\n")
            }
            goSourceCode.append("}\n")
            return goSourceCode.toString()
        }

        private fun generateExpressionCode(expression: Expression): String {
            return when (expression) {
                is Expression.StringConstant -> "\"${expression.value}\"" // TODO escaping
                is Expression.FunctionCall ->
                    expression.function.asIdentifier() + "(" + expression.arguments.joinToString(", ", transform = ::generateExpressionCode) + ")"
                is Expression.VariableReference -> expression.name
            }
        }

        private fun returnType(type: FullyQualifiedType): String? {
            return when (type) {
                Stdlib.void -> null
                else -> nonVoidType(type)
            }
        }

        private fun nonVoidType(type: FullyQualifiedType): String {
            return when (type) {
                Stdlib.void -> throw IllegalArgumentException("Unexpected void type")
                Stdlib.string -> "string"
                else -> throw IllegalArgumentException("Unsupported type ${type}")  // TODO more
            }
        }
    }
}

private fun FullyQualifiedType.asIdentifier() = path.joinToString("_")
