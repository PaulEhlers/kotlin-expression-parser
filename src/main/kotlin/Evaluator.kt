package org.example
import kotlin.math.pow

class EvaluatorException(message: String) : Exception(message)

class Evaluator(
    private val environment: Environment
) {
    companion object {
        fun compile(input: String, environment: Environment?): Double {
            val parser = Parser()
            val ast = parser.parse(input)
            val evaluator = Evaluator(Environment(Environment.default().addParent(environment)))

            return evaluator.evaluate(ast)
        }
    }

    private fun evaluateBinary(operator: TokenType, left: Double, right: Double): Double {
        return when(operator) {
            TokenType.PLUS -> left + right
            TokenType.MINUS -> left - right
            TokenType.MULTIPLY -> left * right
            TokenType.DIVIDE -> left / right
            TokenType.EXPONENTIAL -> left.pow(right)
            else -> throw EvaluatorException("Unrecognized binary operator: $operator")
        }
    }

    fun evaluate(expression: Expression): Double {
        when(expression) {
            is BinaryExpr -> {
                val left = evaluate(expression.left)
                val right = evaluate(expression.right)
                return evaluateBinary(expression.operator.type, left, right)
            }
            is CallExpr -> {
                when(val function = environment.lookup(expression.function)) {
                    is NumberValue -> throw EvaluatorException("Can't use Number as Function")
                    is FunctionValue -> {
                        val evaluatedArguments = expression.arguments.map { evaluate(it) }
                        return function.implementation(evaluatedArguments)
                    }
                }
            }
            is IdentifierExpr -> {
                return when(val lookup = environment.lookup(expression.name)) {
                    is FunctionValue -> throw EvaluatorException("Can't use function as Identifier")
                    is NumberValue -> lookup.value
                }
            }
            is NestedExpr -> {
               return compile(expression.inner, environment)
            }
            is NumberExpr -> return expression.value
        }
    }
}