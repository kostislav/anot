package cz.judas.jan.anot

import cz.judas.jan.anot.ast.FunctionParameter
import cz.judas.jan.anot.ast.FunctionSignature
import cz.judas.jan.anot.ast.typed.Expression
import cz.judas.jan.anot.ast.typed.FullyQualifiedName
import cz.judas.jan.anot.ast.typed.Function
import cz.judas.jan.anot.ast.typed.Package
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.div
import kotlin.io.path.writeText

interface Backend {
    fun compile(source: Package, buildDir: Path, mainFunction: FullyQualifiedName, executableName: String)
}


class CBackend : Backend {
    override fun compile(source: Package, buildDir: Path, mainFunction: FullyQualifiedName, executableName: String) {
        val cSourceCode = StringBuilder("#include <stdio.h>").append("\n\n")

        val generatedStructNames = stdlibStructs.keys
            .withIndex()
            .associate { (i, name) -> name to "S${i}" }
            .toMap()

        val userDefinedFunctions = source.functions().mapValues { UserDefinedFunction(it.value) }
        val allFunctions = stdlibFunctions + userDefinedFunctions
        val generatedFunctionNames = allFunctions.keys
            .withIndex()
            .associate { (i, name) -> name to "f${i}" }
            .toMap()

        val generatedMethodNames = mutableMapOf<Pair<FullyQualifiedName, String>, String>()
        for ((type, struct) in stdlibStructs) {
            for (methodName in struct.methods.keys) {
                generatedMethodNames[type to methodName] = "m${generatedMethodNames.size}"
            }
        }

        for (structName in generatedStructNames.values) {
            cSourceCode.append("typedef struct ${structName} ${structName};\n")
        }

        allFunctions.entries.forEach { (name, function) ->
            cSourceCode.append(generateFunctionDeclaration(function.signature.renamedTo(generatedFunctionNames.getValue(name)), generatedStructNames)).append("\n\n")
        }

        for ((type, struct) in stdlibStructs) {
            for ((methodName, method) in struct.methods) {
                cSourceCode.append(generateFunctionDeclaration(method.signature.renamedTo(generatedMethodNames.getValue(type to methodName)), generatedStructNames)).append("\n\n")
            }
        }

        cSourceCode.append("\n")

        for (structName in generatedStructNames.values) {
            cSourceCode.append("struct ${structName} {};\n")
        }

        allFunctions.entries.forEach { (name, function) ->
            cSourceCode.append(
                function.generateDefinition(
                    generatedFunctionNames.getValue(name),
                    generatedFunctionNames,
                    generatedMethodNames,
                    generatedStructNames
                )
            )
            cSourceCode.append("\n\n")
        }

        for ((type, struct) in stdlibStructs) {
            for ((methodName, method) in struct.methods) {
                cSourceCode.append(
                    method.generateDefinition(
                        generatedMethodNames.getValue(type to methodName),
                        generatedFunctionNames,
                        generatedMethodNames,
                        generatedStructNames,
                    )
                )
                cSourceCode.append("\n\n")
            }
        }

        cSourceCode.append("int main() {\n")
        cSourceCode.append("  ${generatedStructNames.getValue(Stdlib.stdio)} stdio;\n")
        cSourceCode.append("  ")
        cSourceCode.append(generatedFunctionNames.getValue(mainFunction))
        cSourceCode.append("(")
        // TODO more than one
        if (source.functions().getValue(mainFunction).signature.parameters.map { it.type } == listOf(Stdlib.stdio)) {
            cSourceCode.append("&stdio")
        }
        cSourceCode.append(");\n")
        cSourceCode.append("  return 0;\n")
        cSourceCode.append("}")

        val cFile = buildDir / "${executableName}.c"
        val objectFile = buildDir / executableName
        cFile.writeText(cSourceCode.toString())
        runProcess(listOf("gcc", "-s", "-x", "c", "-std=gnu11", "-Wall", "-Werror", cFile.absolutePathString(), "-o", objectFile.absolutePathString()), buildDir)
    }

    private fun runProcess(command: List<String>, workDir: Path) {
        val process = ProcessBuilder(command).directory(workDir.toFile()).start()
        process.waitFor()
        if (process.exitValue() != 0) {
            throw RuntimeException("Backend failed: " + process.errorStream.reader().readText())
        }
    }

    private fun generateFunctionDeclaration(signature: FunctionSignature,  generatedStructNames: Map<FullyQualifiedName, String>): String {
        return buildString {
            append(translateType(signature.returnType, generatedStructNames))
            append(" ")
            append(signature.name)
            append("(")
            append(signature.parameters.joinToString(", ") { translateType(it.type, generatedStructNames) })
            append(");")
        }
    }

    interface CFunction {
        val signature: FunctionSignature

        fun generateDefinition(
            functionName: String,
            generatedFunctionNames: Map<FullyQualifiedName, String>,
            generatedMethodNames: Map<Pair<FullyQualifiedName, String>, String>,
            generatedStructNames: Map<FullyQualifiedName, String>,
        ): String
    }

    object Stdio {
        val methods = mapOf("println" to Println)
    }

    object Println : CFunction {
        override val signature = FunctionSignature("println", listOf(FunctionParameter("this", Stdlib.stdio), FunctionParameter("value", Stdlib.string)), Stdlib.void)

        override fun generateDefinition(
            functionName: String,
            generatedFunctionNames: Map<FullyQualifiedName, String>,
            generatedMethodNames: Map<Pair<FullyQualifiedName, String>, String>,
            generatedStructNames: Map<FullyQualifiedName, String>,
        ): String {
            return """
            void ${functionName}(${translateType(Stdlib.stdio, generatedStructNames)} stdio, const char *arg) {
                puts(arg);
            }
            """.trimIndent()
        }
    }

    companion object {
        val stdlibStructs = mapOf(
            Stdlib.stdio to Stdio
        )

        val stdlibFunctions = emptyMap<FullyQualifiedName, CFunction>()
    }

    class UserDefinedFunction(private val function: Function) : CFunction {
        override val signature = function.signature

        override fun generateDefinition(
            functionName: String,
            generatedFunctionNames: Map<FullyQualifiedName, String>,
            generatedMethodNames: Map<Pair<FullyQualifiedName, String>, String>,
            generatedStructNames: Map<FullyQualifiedName, String>,
        ): String {
            val cSourceCode = StringBuilder("${translateType(function.signature.returnType, generatedStructNames)} ${functionName}(")
            cSourceCode.append(function.signature.parameters.joinToString(", ") { "${translateType(it.type, generatedStructNames)} ${it.name}" })
            cSourceCode.append(") {\n")
            val bodyIterator = function.body.iterator()
            while (bodyIterator.hasNext()) {
                val expression = bodyIterator.next()
                cSourceCode.append("\t")
                if (function.signature.returnType != Stdlib.void && !bodyIterator.hasNext()) {
                    cSourceCode.append("return ")
                }
                cSourceCode.append(generateExpressionCode(expression, generatedFunctionNames, generatedMethodNames)).append(";\n")
            }
            cSourceCode.append("}\n")
            return cSourceCode.toString()
        }

        private fun generateExpressionCode(
            expression: Expression,
            generatedFunctionNames: Map<FullyQualifiedName, String>,
            generatedMethodNames: Map<Pair<FullyQualifiedName, String>, String>,
        ): String {
            return when (expression) {
                is Expression.StringConstant -> "\"${expression.value}\"" // TODO escaping
                is Expression.FunctionCall -> buildString {
                    append(generatedFunctionNames.getValue(expression.function))
                    append("(")
                    append(expression.arguments.joinToString(", ") { generateExpressionCode(it, generatedFunctionNames, generatedMethodNames) })
                    append(")")
                }

                is Expression.MethodCall -> buildString {
                    append(generatedMethodNames.getValue(expression.receiver.type to expression.methodName))
                    append("(")
                    append(expression.receiver.name)
                    append(", ")
                    append(expression.arguments.joinToString(", ") { generateExpressionCode(it, generatedFunctionNames, generatedMethodNames) })
                    append(")")
                }

                is Expression.VariableReference -> expression.name
            }
        }

    }

}

private fun translateType(type: FullyQualifiedName, generatedStructNames: Map<FullyQualifiedName, String>): String {
    return when (type) {
        Stdlib.void -> "void"
        Stdlib.string -> "const char *"
        else -> "const ${generatedStructNames.getValue(type)} *"
    }
}
