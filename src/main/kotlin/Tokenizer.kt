package org.example

enum class TokenType {
    NUMBER,
    IDENTIFIER,
    STRING,
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,
    PARENTHESES_OPEN,
    PARENTHESES_CLOSE,
    COMMA,
    EXPONENTIAL,
}

data class Token(
    val type: TokenType,
    val value: String,
)

class TokenizerException(message: String) : Exception()

class Tokenizer() {
    var cursor : Int = 0
    var input : String = ""

    fun read(input: String) {
        this.input = input
        this.cursor = 0
    }

    fun next() : Token? {
        skipWhitespace()

        if (this.cursor >= this.input.length) {
            return null
        }


        val currentChar = getCurrentChar()

        val singleDigitToken = processSingleCharacterToken()
        if(singleDigitToken != null) {
            consumeChar()
            return singleDigitToken
        }

        return when {
            currentChar.isDigit() -> Token(
                type = TokenType.NUMBER,
                value = processNumber()
            )
            currentChar == '\"' -> Token(
                type = TokenType.STRING,
                value = processString()
            )
            currentChar.isLetter() -> Token(
                type = TokenType.IDENTIFIER,
                value = processIdentifier()
            )
            else -> throw TokenizerException("Unknown character: '$currentChar'")
        }
    }

    private fun processSingleCharacterToken(): Token? {
        val currentChar = getCurrentChar()
        return when(currentChar) {
            '+' -> Token(type = TokenType.PLUS, value = "+")
            '-' -> Token(type = TokenType.MINUS, value = "-")
            '/' -> Token(type = TokenType.DIVIDE, value = "/")
            '*' -> Token(type = TokenType.MULTIPLY, value = "*")
            '^' -> Token(type = TokenType.EXPONENTIAL, value = "^")
            '(' -> Token(type = TokenType.PARENTHESES_OPEN, value = "(")
            ')' -> Token(type = TokenType.PARENTHESES_CLOSE, value = ")")
            ',' -> Token(type = TokenType.COMMA, value = ",")
            else -> null
        }
    }

    private fun getCurrentChar() : Char {
        if (cursor >= input.length) {
            throw TokenizerException("Unexpected end of input while reading character.")
        }

        return this.input[this.cursor]
    }

    private fun consumeChar() : Char {
        if (cursor >= input.length) {
                throw TokenizerException("Unexpected end of input while reading character.")
        }

        val char = getCurrentChar()
        this.cursor++
        return char
    }

    private fun peekNext() : Char? {
        return this.input.getOrNull(this.cursor+1)
    }

    private fun skipWhitespace() {
        while (cursor < input.length && getCurrentChar() == ' ') {
            consumeChar()
        }
    }

    private fun processNumber() : String {
        var number : StringBuilder = StringBuilder()
        var hasPoint = false

        while(cursor < input.length  && (getCurrentChar().isDigit() || getCurrentChar() == '.')) {
            if(getCurrentChar() == '.') {
                if(hasPoint) {
                    throw TokenizerException("Invalid digit format (Multiple points)")
                }
                hasPoint = true
            }

            number.append(consumeChar())
        }

        return number.toString()
    }

    private fun processString() : String {
        consumeChar()
        val string : StringBuilder = StringBuilder()

        while(getCurrentChar() != '\"') {
            if(cursor >= input.length) {
                throw TokenizerException("Missing closing \" in string")
            }
            string.append(consumeChar().toString())
        }
        consumeChar()

        return string.toString()
    }

    private fun processIdentifier(): String {
        val identifier = StringBuilder()

        while (cursor < input.length && getCurrentChar().isLetter()) {
            identifier.append(consumeChar())
        }

        return identifier.toString()
    }
}