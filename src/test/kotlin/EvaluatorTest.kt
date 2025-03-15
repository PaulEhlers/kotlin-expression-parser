package org.example

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EvaluatorTest {

    private val defaultEnv = Environment.default()

    @Test
    fun `test simple number`() {
        val result = Evaluator.compile("42", defaultEnv)
        assertEquals(42.0, result)
    }

    @Test
    fun `test simple addition`() {
        val result = Evaluator.compile("1 + 2", defaultEnv)
        assertEquals(3.0, result)
    }

    @Test
    fun `test operator precedence`() {
        val result = Evaluator.compile("2 + 3 * 4", defaultEnv)
        assertEquals(14.0, result)
    }

    @Test
    fun `test parentheses grouping`() {
        val result = Evaluator.compile("(2 + 3) * 4", defaultEnv)
        assertEquals(20.0, result)
    }

    @Test
    fun `test exponentiation`() {
        val result = Evaluator.compile("2 ^ 3", defaultEnv)
        assertEquals(8.0, result)
    }

    @Test
    fun `test exponentiation2`() {
        val result = Evaluator.compile("2 ^ 2", defaultEnv)
        assertEquals(4.0, result)
    }

    @Test
    fun `test sin function`() {
        val result = Evaluator.compile("sin(pi / 2)", defaultEnv)
        assertEquals(1.0, result, 0.0001) // Toleranz wegen Rundung
    }

    @Test
    fun `test sqrt function`() {
        val result = Evaluator.compile("sqrt(16)", defaultEnv)
        assertEquals(4.0, result)
    }

    @Test
    fun `test log function`() {
        val result = Evaluator.compile("log(8, 2)", defaultEnv)
        assertEquals(3.0, result)
    }

    @Test
    fun `test max function`() {
        val result = Evaluator.compile("max(10, 20)", defaultEnv)
        assertEquals(20.0, result)
    }

    @Test
    fun `test min function`() {
        val result = Evaluator.compile("min(10, 20)", defaultEnv)
        assertEquals(10.0, result)
    }

    @Test
    fun `test constant pi`() {
        val result = Evaluator.compile("pi", defaultEnv)
        assertEquals(Math.PI, result)
    }

    @Test
    fun `test nested expression`() {
        val result = Evaluator.compile("\"2 + 3\"", defaultEnv)
        assertEquals(5.0, result)
    }

    @Test
    fun `test nested expression with function`() {
        val result = Evaluator.compile("\"sqrt(81)\"", defaultEnv)
        assertEquals(9.0, result)
    }

    @Test
    fun `test invalid function usage`() {
        val ex = assertThrows<RuntimeException> {
            Evaluator.compile("42(1)", defaultEnv)
        }
    }

    @Test
    fun `test function as identifier error`() {
        val ex = assertThrows<EvaluatorException> {
            Evaluator.compile("sqrt", defaultEnv)
        }
        assertEquals("Can't use function as Identifier", ex.message)
    }

    @Test
    fun `test unknown identifier`() {
        val ex = assertThrows<RuntimeException> {
            Evaluator.compile("unknownVar", defaultEnv)
        }
        assertEquals("Unknown identifier \"unknownVar\"", ex.message)
    }

    @Test
    fun `test complex expression`() {
        val result = Evaluator.compile("sqrt(16) + log(8, 2) * 2 ^ 2", defaultEnv)
        assertEquals(4.0 + 3.0 * 4.0, result)
    }
}
