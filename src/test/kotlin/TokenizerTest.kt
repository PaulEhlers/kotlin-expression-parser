package org.example

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TokenizerTest {

    @Test
    fun `test simple number token`() {
        val tokenizer = Tokenizer()
        tokenizer.read("42")

        val token = tokenizer.next()
        assertNotNull(token)
        assertEquals(TokenType.NUMBER, token!!.type)
        assertEquals("42", token.value)

        assertNull(tokenizer.next())
    }

    @Test
    fun `test simple identifier`() {
        val tokenizer = Tokenizer()
        tokenizer.read("x")

        val token = tokenizer.next()
        assertNotNull(token)
        assertEquals(TokenType.IDENTIFIER, token!!.type)
        assertEquals("x", token.value)

        assertNull(tokenizer.next())
    }

    @Test
    fun `test simple string`() {
        val tokenizer = Tokenizer()
        tokenizer.read("\"hello\"")

        val token = tokenizer.next()
        assertNotNull(token)
        assertEquals(TokenType.STRING, token!!.type)
        assertEquals("hello", token.value)

        assertNull(tokenizer.next())
    }

    @Test
    fun `test multiple tokens in expression`() {
        val tokenizer = Tokenizer()
        tokenizer.read("4 + 3 * (x - 2)")

        val expected = listOf(
            Token(TokenType.NUMBER, "4"),
            Token(TokenType.PLUS, "+"),
            Token(TokenType.NUMBER, "3"),
            Token(TokenType.MULTIPLY, "*"),
            Token(TokenType.PARENTHESES_OPEN, "("),
            Token(TokenType.IDENTIFIER, "x"),
            Token(TokenType.MINUS, "-"),
            Token(TokenType.NUMBER, "2"),
            Token(TokenType.PARENTHESES_CLOSE, ")"),
        )

        expected.forEach { expectedToken ->
            val token = tokenizer.next()
            assertNotNull(token)
            assertEquals(expectedToken.type, token!!.type)
            assertEquals(expectedToken.value, token.value)
        }

        assertNull(tokenizer.next())
    }

    @Test
    fun `test whitespace handling`() {
        val tokenizer = Tokenizer()
        tokenizer.read("  7    *   8 ")

        val expected = listOf(
            Token(TokenType.NUMBER, "7"),
            Token(TokenType.MULTIPLY, "*"),
            Token(TokenType.NUMBER, "8"),
        )

        expected.forEach { expectedToken ->
            val token = tokenizer.next()
            assertNotNull(token)
            assertEquals(expectedToken.type, token!!.type)
            assertEquals(expectedToken.value, token.value)
        }

        assertNull(tokenizer.next())
    }

    @Test
    fun `test number with decimal`() {
        val tokenizer = Tokenizer()
        tokenizer.read("3.14 + 2.71")

        val expected = listOf(
            Token(TokenType.NUMBER, "3.14"),
            Token(TokenType.PLUS, "+"),
            Token(TokenType.NUMBER, "2.71"),
        )

        expected.forEach { expectedToken ->
            val token = tokenizer.next()
            assertNotNull(token)
            assertEquals(expectedToken.type, token!!.type)
            assertEquals(expectedToken.value, token.value)
        }

        assertNull(tokenizer.next())
    }

    @Test
    fun `test function call with comma`() {
        val tokenizer = Tokenizer()
        tokenizer.read("max(3, 5)")

        val expected = listOf(
            Token(TokenType.IDENTIFIER, "max"),
            Token(TokenType.PARENTHESES_OPEN, "("),
            Token(TokenType.NUMBER, "3"),
            Token(TokenType.COMMA, ","),
            Token(TokenType.NUMBER, "5"),
            Token(TokenType.PARENTHESES_CLOSE, ")"),
        )

        expected.forEach { expectedToken ->
            val token = tokenizer.next()
            assertNotNull(token)
            assertEquals(expectedToken.type, token!!.type)
            assertEquals(expectedToken.value, token.value)
        }

        assertNull(tokenizer.next())
    }

    @Test
    fun `test exponential`() {
        val tokenizer = Tokenizer()
        tokenizer.read("2 ^ 8")

        val expected = listOf(
            Token(TokenType.NUMBER, "2"),
            Token(TokenType.EXPONENTIAL, "^"),
            Token(TokenType.NUMBER, "8"),
        )

        expected.forEach { expectedToken ->
            val token = tokenizer.next()
            assertNotNull(token)
            assertEquals(expectedToken.type, token!!.type)
            assertEquals(expectedToken.value, token.value)
        }

        assertNull(tokenizer.next())
    }

    @Test
    fun `test invalid number with two dots throws exception`() {
        val tokenizer = Tokenizer()
        tokenizer.read("3.14.15")

        assertThrows<TokenizerException> {
            while (tokenizer.next() != null) { /* Parse everything */ }
        }
    }

    @Test
    fun `test unterminated string throws exception`() {
        val tokenizer = Tokenizer()
        tokenizer.read("\"hello")

        assertThrows<TokenizerException> {
            tokenizer.next()
        }
    }
}
