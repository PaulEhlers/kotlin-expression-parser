package org.example

/**
 * Base class for all expressions in the AST.
 */
sealed class Expression

/**
 * Represents a numeric literal.
 *
 * @property value the numeric value
 */
data class NumberExpr(val value: Double) : Expression()

/**
 * Represents an identifier.
 *
 * @property name the identifier name
 */
data class IdentifierExpr(val name: String) : Expression()

/**
 * Represents a binary expression.
 *
 * @property left the left operand
 * @property operator the operator token
 * @property right the right operand
 */
data class BinaryExpr(
    val left: Expression,
    val operator: Token,
    val right: Expression
) : Expression()

/**
 * Represents a function call.
 *
 * @property function the function name
 * @property arguments the list of argument expressions
 */
data class CallExpr(
    val function: String,
    val arguments: List<Expression>
) : Expression()

/**
 * Represents a nested expression contained in a string.
 *
 * @property inner the inner expression string
 */
data class NestedExpr(val inner: String) : Expression()

/**
 * Exception thrown during parsing errors.
 *
 * @param message the error message
 */
class ParseException(message: String) : Exception(message)

/**
 * Parses a mathematical expression string into an AST.
 *
 * @property tokenizer the tokenizer used to generate tokens
 */
class Parser(
    private val tokenizer: Tokenizer = Tokenizer(),
    private var lookahead: Token? = null
) {

    /**
     * Parses the input string into an Expression AST.
     *
     * @param input the string to parse
     * @return the parsed Expression
     * @throws ParseException if input is empty or invalid
     */
    fun parse(input: String): Expression {
        tokenizer.read(input)
        lookahead = tokenizer.next()
        if (lookahead == null) throw ParseException("Input is empty or invalid.")
        return parseExpression()
    }

    /**
     * Consumes the next token if its type is among the expected types.
     *
     * @param tokenTypes the acceptable token types
     * @return the consumed token
     * @throws ParseException if token type does not match or end of input is reached
     */
    private fun eat(vararg tokenTypes: TokenType): Token {
        val token = lookahead ?: throw ParseException("Unexpected end of input.")
        if (!isType(*tokenTypes)) throw ParseException("Expected one of: ${tokenTypes.joinToString()} but got: ${token.type} (${token.value})")
        lookahead = tokenizer.next()
        return token
    }

    /**
     * Checks if the current lookahead token is one of the given types.
     *
     * @param tokenTypes the token types to check against
     * @return true if current token matches, false otherwise
     */
    private fun isType(vararg tokenTypes: TokenType): Boolean {
        return tokenTypes.contains(lookahead?.type)
    }

    /**
     * Parses an expression starting with addition/subtraction.
     *
     * @return the parsed Expression
     */
    private fun parseExpression(): Expression = parseAddition()

    /**
     * Parses addition and subtraction expressions.
     */
    private fun parseAddition(): Expression {
        var left = parseMultiplication()
        while (isType(TokenType.PLUS, TokenType.MINUS)) {
            left = BinaryExpr(
                left = left,
                operator = eat(TokenType.PLUS, TokenType.MINUS),
                right = parseMultiplication()
            )
        }
        return left
    }

    /**
     * Parses a function call if applicable.
     *
     * Handles both implicit and explicit calls.
     *
     * @return the parsed Expression (either a call or a basic expression)
     */
    private fun parseCall(): Expression {
        val callee = parseBasic()
        val calleeValue: String? = when(callee) {
            is BinaryExpr, is CallExpr, is NestedExpr -> null
            is IdentifierExpr -> callee.name
            is NumberExpr -> callee.value.toString()
        }

        if (isType(TokenType.NUMBER, TokenType.IDENTIFIER)) {
            return CallExpr(
                function = calleeValue!!,
                arguments = listOf(parseCall())
            )
        }

        if (isType(TokenType.PARENTHESES_OPEN)) {
            eat(TokenType.PARENTHESES_OPEN)
            val arguments = mutableListOf(parseExpression())
            while (isType(TokenType.COMMA)) {
                eat(TokenType.COMMA)
                arguments.add(parseExpression())
            }
            eat(TokenType.PARENTHESES_CLOSE)
            return CallExpr(function = calleeValue!!, arguments = arguments)
        }

        return callee
    }

    /**
     * Parses multiplication and division expressions.
     */
    private fun parseMultiplication(): Expression {
        var left = parseExponentiation()
        while (isType(TokenType.MULTIPLY, TokenType.DIVIDE)) {
            left = BinaryExpr(
                left = left,
                operator = eat(TokenType.MULTIPLY, TokenType.DIVIDE),
                right = parseExponentiation()
            )
        }
        return left
    }

    /**
     * Parses exponentiation expressions.
     *
     * Exponentiation is treated as right-associative.
     */
    private fun parseExponentiation(): Expression {
        var left = parseCall()
        while (isType(TokenType.EXPONENTIAL)) {
            left = BinaryExpr(
                left = left,
                operator = eat(TokenType.EXPONENTIAL),
                right = parseExponentiation()
            )
        }
        return left
    }

    /**
     * Parses basic expressions: numbers, identifiers, parenthesized or nested expressions.
     *
     * @return the basic Expression
     * @throws ParseException if the expression is unrecognized
     */
    private fun parseBasic(): Expression {
        if (isType(TokenType.PARENTHESES_OPEN)) {
            eat(TokenType.PARENTHESES_OPEN)
            val expression = parseExpression()
            eat(TokenType.PARENTHESES_CLOSE)
            return expression
        }
        if (isType(TokenType.NUMBER)) {
            return NumberExpr(eat(TokenType.NUMBER).value.toDouble())
        }
        if (isType(TokenType.IDENTIFIER)) {
            return IdentifierExpr(eat(TokenType.IDENTIFIER).value)
        }
        if (isType(TokenType.STRING)) {
            val expression = eat(TokenType.STRING).value
            return NestedExpr(inner = expression)
        }
        throw ParseException("Unknown expression")
    }
}
