package cz.judas.jan.jazyk2

import cz.judas.jan.jazyk2.ast.typed.Expression
import cz.judas.jan.jazyk2.ast.typed.FullyQualifiedType
import cz.judas.jan.jazyk2.ast.typed.Function
import cz.judas.jan.jazyk2.ast.typed.Package
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.div
import kotlin.io.path.writeText

interface Backend {
    fun compile(source: Package, buildDir: Path, mainFunction: FullyQualifiedType, executableName: String)
}


class CBackend : Backend {
    override fun compile(source: Package, buildDir: Path, mainFunction: FullyQualifiedType, executableName: String) {
        val cSourceCode = StringBuilder("#include <stdio.h>").append("\n\n")

        val userDefinedFunctions = source.functions.associate { it.name to UserDefinedFunction(it) }.toMap()
        val allFunctions = stdlibCImpl + userDefinedFunctions
        val generatedFunctionNames = allFunctions.keys
            .withIndex()
            .associate { (i, name) -> name to "f${i}" }
            .toMap()

        allFunctions.entries.forEach { (name, function) ->
            cSourceCode.append(function.generateDeclaration(generatedFunctionNames.getValue(name))).append("\n\n")
        }

        cSourceCode.append("\n")

        allFunctions.entries.forEach { (name, function) ->
            cSourceCode.append(function.generateDefinition(generatedFunctionNames.getValue(name), generatedFunctionNames)).append("\n\n")
        }

        cSourceCode.append(
            """
            void main() {
                ${generatedFunctionNames.getValue(mainFunction)}();
            }
            
        """.trimIndent()
        )

        val cFile = buildDir / "${executableName}.c"
        val objectFile = buildDir / executableName
        cFile.writeText(cSourceCode.toString())
        runProcess(listOf("gcc", "-s", "-x", "c", "-std=gnu11", cFile.absolutePathString(), "-o", objectFile.absolutePathString()), buildDir)
        runProcess(listOf("strip", "-R", ".comment", "-R", ".note", objectFile.absolutePathString()), buildDir)
    }

    private fun runProcess(command: List<String>, workDir: Path) {
        val process = ProcessBuilder(command).directory(workDir.toFile()).start()
        process.waitFor()
        if (process.exitValue() != 0) {
            throw RuntimeException("Backend failed: " + process.errorStream.reader().readText())
        }
    }

    interface CFunction {
        fun generateDeclaration(functionName: String): String

        fun generateDefinition(functionName: String, generatedFunctionNames: Map<FullyQualifiedType, String>): String
    }

    object Println: CFunction {
        override fun generateDeclaration(functionName: String): String {
            return "void ${functionName}(const char *);"
        }

        override fun generateDefinition(functionName: String, generatedFunctionNames: Map<FullyQualifiedType, String>): String {
            return """
            void ${functionName}(const char *arg) {
                puts(arg);
            }
            """.trimIndent()
        }
    }

    companion object {
        val stdlibCImpl = mapOf(
            Stdlib.println to Println
        )
    }

    class UserDefinedFunction(private val function: Function) : CFunction {
        override fun generateDeclaration(functionName: String): String {
            return "${translateType(function.returnType)} ${functionName}(${function.parameters.joinToString(",") { translateType(it.type) }});"
        }

        override fun generateDefinition(functionName: String, generatedFunctionNames: Map<FullyQualifiedType, String>): String {
            val cSourceCode = StringBuilder("${translateType(function.returnType)} ${functionName}(")
            cSourceCode.append(function.parameters.joinToString(",") { "${translateType(it.type)} ${it.name}" })
            cSourceCode.append(") {\n")
            val bodyIterator = function.body.iterator()
            while (bodyIterator.hasNext()) {
                val expression = bodyIterator.next()
                cSourceCode.append("\t")
                if (function.returnType != Stdlib.void && !bodyIterator.hasNext()) {
                    cSourceCode.append("return ")
                }
                cSourceCode.append(generateExpressionCode(expression, generatedFunctionNames)).append(";\n")
            }
            cSourceCode.append("}\n")
            return cSourceCode.toString()
        }

        private fun generateExpressionCode(expression: Expression, generatedFunctionNames: Map<FullyQualifiedType, String>): String {
            return when (expression) {
                is Expression.StringConstant -> "\"${expression.value}\"" // TODO escaping
                is Expression.FunctionCall ->
                    generatedFunctionNames.getValue(expression.function) + "(" + expression.arguments.joinToString(", ") { generateExpressionCode(it, generatedFunctionNames) } + ")"
                is Expression.VariableReference -> expression.name
            }
        }

        private fun translateType(type: FullyQualifiedType): String {
            return when (type) {
                Stdlib.void -> "void"
                Stdlib.string -> "const char *"
                else -> throw IllegalArgumentException("Unsupported type ${type}")  // TODO more
            }
        }
    }
}
