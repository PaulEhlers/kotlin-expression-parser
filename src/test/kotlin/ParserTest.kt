package org.example

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ParserTest {

    @Test
    fun `test simple number`() {
        val expr = Parser().parse("42")
        assertTrue(expr is NumberExpr)
        assertEquals(42.0, (expr as NumberExpr).value)
    }

    @Test
    fun `test simple identifier`() {
        val expr = Parser().parse("x")
        assertTrue(expr is IdentifierExpr)
        assertEquals("x", (expr as IdentifierExpr).name)
    }

    @Test
    fun `test simple addition`() {
        val expr = Parser().parse("1 + 2")
        assertTrue(expr is BinaryExpr)
        val binary = expr as BinaryExpr
        assertEquals(TokenType.PLUS, binary.operator.type)
        assertEquals(1.0, (binary.left as NumberExpr).value)
        assertEquals(2.0, (binary.right as NumberExpr).value)
    }

    @Test
    fun `test operator precedence`() {
        val expr = Parser().parse("1 + 2 * 3")
        assertTrue(expr is BinaryExpr)
        val add = expr as BinaryExpr
        assertEquals(TokenType.PLUS, add.operator.type)
        assertEquals(1.0, (add.left as NumberExpr).value)

        assertTrue(add.right is BinaryExpr)
        val multiply = add.right as BinaryExpr
        assertEquals(TokenType.MULTIPLY, multiply.operator.type)
        assertEquals(2.0, (multiply.left as NumberExpr).value)
        assertEquals(3.0, (multiply.right as NumberExpr).value)
    }

    @Test
    fun `test exponentiation`() {
        val expr = Parser().parse("2 ^ 3 ^ 4")
        assertTrue(expr is BinaryExpr)
        val first = expr as BinaryExpr
        assertEquals(TokenType.EXPONENTIAL, first.operator.type)
        assertEquals(2.0, (first.left as NumberExpr).value)

        assertTrue(first.right is BinaryExpr)
        val second = first.right as BinaryExpr
        assertEquals(TokenType.EXPONENTIAL, second.operator.type)
        assertEquals(3.0, (second.left as NumberExpr).value)
        assertEquals(4.0, (second.right as NumberExpr).value)
    }

    @Test
    fun `test parentheses grouping`() {
        val expr = Parser().parse("(1 + 2) * 3")
        assertTrue(expr is BinaryExpr)
        val multiply = expr as BinaryExpr
        assertEquals(TokenType.MULTIPLY, multiply.operator.type)
        assertEquals(3.0, (multiply.right as NumberExpr).value)

        assertTrue(multiply.left is BinaryExpr)
        val addition = multiply.left as BinaryExpr
        assertEquals(TokenType.PLUS, addition.operator.type)
        assertEquals(1.0, (addition.left as NumberExpr).value)
        assertEquals(2.0, (addition.right as NumberExpr).value)
    }

    @Test
    fun `test function call with args`() {
        val expr = Parser().parse("max(1, 2)")
        assertTrue(expr is CallExpr)
        val call = expr as CallExpr
        assertEquals("max", call.function)
        assertEquals(2, call.arguments.size)
        assertEquals(1.0, (call.arguments[0] as NumberExpr).value)
        assertEquals(2.0, (call.arguments[1] as NumberExpr).value)
    }

    @Test
    fun `test nested function call`() {
        val expr = Parser().parse("max(min(1, 2), 3)")
        assertTrue(expr is CallExpr)
        val outerCall = expr as CallExpr
        assertEquals("max", outerCall.function)

        assertTrue(outerCall.arguments[0] is CallExpr)
        val innerCall = outerCall.arguments[0] as CallExpr
        assertEquals("min", innerCall.function)

        assertEquals(1.0, (innerCall.arguments[0] as NumberExpr).value)
        assertEquals(2.0, (innerCall.arguments[1] as NumberExpr).value)

        assertEquals(3.0, (outerCall.arguments[1] as NumberExpr).value)
    }

    @Test
    fun `test embedded string expression`() {
        val expr = Parser().parse("\"1 + 2\"")
        assertTrue(expr is NestedExpr)
        val innerExpr = (expr as NestedExpr).inner
        assertEquals("1 + 2", innerExpr)
    }

    @Test
    fun `test complex expression`() {
        val expr = Parser().parse("2 + 3 * 4 ^ 2")
        assertTrue(expr is BinaryExpr)
        val add = expr as BinaryExpr
        assertEquals(TokenType.PLUS, add.operator.type)
        assertEquals(2.0, (add.left as NumberExpr).value)

        val multiply = add.right as BinaryExpr
        assertEquals(TokenType.MULTIPLY, multiply.operator.type)
        assertEquals(3.0, (multiply.left as NumberExpr).value)

        val exp = multiply.right as BinaryExpr
        assertEquals(TokenType.EXPONENTIAL, exp.operator.type)
        assertEquals(4.0, (exp.left as NumberExpr).value)
        assertEquals(2.0, (exp.right as NumberExpr).value)
    }

    @Test
    fun `test sigma function call`() {
        val expr = Parser().parse("sigma(\"x\", 1, 5)")
        assertTrue(expr is CallExpr)
        val call = expr as CallExpr
        assertEquals("sigma", call.function)

        assertEquals(3, call.arguments.size)

        val arg1 = call.arguments[0]
        assertTrue(arg1 is NestedExpr)
        val inner1 = (arg1 as NestedExpr).inner
        assertEquals("x", inner1)

        val arg2 = call.arguments[1]
        assertTrue(arg2 is NumberExpr)
        assertEquals(1.0, (arg2 as NumberExpr).value)

        val arg3 = call.arguments[2]
        assertTrue(arg3 is NumberExpr)
        assertEquals(5.0, (arg3 as NumberExpr).value)
    }


    @Test
    fun `test empty input throws exception`() {
        assertThrows<ParseException> {
            Parser().parse("")
        }
    }

    @Test
    fun `test unmatched parentheses throws exception`() {
        assertThrows<ParseException> {
            Parser().parse("(1 + 2")
        }
    }

    @Test
    fun `test unknown token throws exception`() {
        assertThrows<TokenizerException> {
            Parser().parse("@")
        }
    }
}
