package org.example

sealed class Expression

data class NumberExpr(val value: String) : Expression()

data class IdentifierExpr(val name: String) : Expression()

data class BinaryExpr(
    val left: Expression,
    val operator: Token,
    val right: Expression
) : Expression()

data class CallExpr(
    val function: Expression,
    val arguments: List<Expression>
) : Expression()

data class NestedExpr(val inner: Expression) : Expression()

class ParseException(message: String) : Exception(message)

class Parser(
    private val tokenizer: Tokenizer = Tokenizer(),
    private var lookahead: Token? = null
) {

    fun parse(input: String) : Expression  {
        tokenizer.read(input)
        lookahead = tokenizer.next()
        if (lookahead == null) throw ParseException("Input is empty or invalid.")
        return parseExpression()
    }

    private fun eat(vararg tokenTypes: TokenType) : Token {
        val token = lookahead ?: throw ParseException("Unexpected end of input. ")
        if (!isType(*tokenTypes)) throw ParseException("Expected one of: ${tokenTypes.joinToString()} but got: ${token.type} (${token.value})")

        lookahead = tokenizer.next()

        return token
    }

    private fun isType(vararg tokenTypes: TokenType): Boolean {
        return tokenTypes.contains(lookahead?.type)
    }

    private fun parseExpression() : Expression {
        return this.parseAddition()
    }

    private fun parseAddition() : Expression {
        var left = parseCall()

        while (isType(TokenType.PLUS, TokenType.MINUS)) {
            left = BinaryExpr(
                left = left,
                operator = eat(TokenType.PLUS, TokenType.MINUS),
                right = parseCall()
            )
        }

        return left
    }

    private fun parseCall() : Expression {
        val callee = parseMultiplication()

        if (isType(TokenType.NUMBER, TokenType.IDENTIFIER)) {
            return CallExpr(
                function = callee,
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

            this.eat(TokenType.PARENTHESES_CLOSE)
            return CallExpr(function = callee, arguments = arguments)
        }

        return callee
    }

    private fun parseMultiplication() : Expression {
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

    private fun parseExponentiation() : Expression {
        var left = parseBasic()

        while (isType(TokenType.EXPONENTIAL)) {
            left = BinaryExpr(
                left = left,
                operator = eat(TokenType.EXPONENTIAL),
                right = parseExponentiation()
            )
        }

        return left
    }

    private fun parseBasic(): Expression {
        if (isType(TokenType.PARENTHESES_OPEN)) {
            eat(TokenType.PARENTHESES_OPEN)
            val expression = parseExpression()
            eat(TokenType.PARENTHESES_CLOSE)

            return expression
        }

        if (isType(TokenType.NUMBER)) {
            return NumberExpr(
                value = eat(TokenType.NUMBER).value
            )
        }

        if(isType(TokenType.IDENTIFIER)) {
            return IdentifierExpr(
                name = eat(TokenType.IDENTIFIER).value
            )
        }

        if(isType(TokenType.STRING)) {
            val expression = eat(TokenType.STRING).value
            val innerExpression = Parser().parse(expression)

            return NestedExpr(inner = innerExpression)
        }

        throw ParseException("Unknown expression")
    }

}