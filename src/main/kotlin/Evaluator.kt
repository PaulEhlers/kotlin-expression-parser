package org.example
import kotlin.math.pow

/**
 * Exception thrown during evaluation errors.
 *
 * @param message a descriptive error message.
 */
class EvaluatorException(message: String) : Exception(message)

/**
 * Evaluates mathematical expressions parsed into an AST.
 *
 * @property environment the current evaluation environment.
 */
class Evaluator(
    private val environment: Environment
) {
    companion object {
        /**
         * Parses and evaluates an input expression.
         *
         * @param input the expression string to compile
         * @param environment an optional environment for variable/function lookup
         * @return the evaluated result as a Double
         */
        fun compile(input: String, environment: Environment?): Double {
            val parser = Parser()
            val ast = parser.parse(input)
            val evaluator = Evaluator(Environment(Environment.default().addParent(environment)))
            return evaluator.evaluate(ast)
        }
    }

    /**
     * Evaluates a binary operation.
     *
     * @param operator the binary operator token type
     * @param left the left operand
     * @param right the right operand
     * @return the computed result
     * @throws EvaluatorException for unknown operators.
     */
    private fun evaluateBinary(operator: TokenType, left: Double, right: Double): Double {
        return when (operator) {
            TokenType.PLUS -> left + right
            TokenType.MINUS -> left - right
            TokenType.MULTIPLY -> left * right
            TokenType.DIVIDE -> left / right
            TokenType.EXPONENTIAL -> left.pow(right)
            else -> throw EvaluatorException("Unrecognized binary operator: $operator")
        }
    }

    /**
     * Recursively evaluates an expression AST.
     *
     * Supports binary expressions, function calls, identifiers, nested expressions,
     * and number literals.
     *
     * @param expression the expression AST node
     * @return the evaluated Double result
     * @throws EvaluatorException for invalid function/identifier usage.
     */
    fun evaluate(expression: Expression): Double {
        return when (expression) {
            is BinaryExpr -> {
                val left = evaluate(expression.left)
                val right = evaluate(expression.right)
                evaluateBinary(expression.operator.type, left, right)
            }
            is CallExpr -> {
                when (val function = environment.lookup(expression.function)) {
                    is NumberValue -> throw EvaluatorException("Can't use Number as Function")
                    is FunctionValue -> {
                        val evaluatedArguments = expression.arguments.map { evaluate(it) }
                        function.implementation(evaluatedArguments)
                    }
                    else -> throw EvaluatorException("Invalid function type")
                }
            }
            is IdentifierExpr -> {
                when (val lookup = environment.lookup(expression.name)) {
                    is FunctionValue -> throw EvaluatorException("Can't use function as Identifier")
                    is NumberValue -> lookup.value
                    else -> throw EvaluatorException("Invalid identifier type")
                }
            }
            is NestedExpr -> compile(expression.inner, environment)
            is NumberExpr -> expression.value
        }
    }
}
