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
        assertEquals(1.0, result, 0.0001)
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

    @Test
    fun `test left-associative division`() {
        val result = Evaluator.compile("10 / 2 / 5", defaultEnv)
        assertEquals(1.0, result)
    }

    @Test
    fun `test left-associative subtraction`() {
        val result = Evaluator.compile("10 - 2 - 3", defaultEnv)
        assertEquals(5.0, result)
    }

    @Test
    fun `test exponentiation associativity`() {
        val result = Evaluator.compile("2 ^ 3 ^ 2", defaultEnv)
        assertEquals(512.0, result)
    }

    @Test
    fun `test division by zero`() {
        val result = Evaluator.compile("1 / 0", defaultEnv)
        assertEquals(Double.POSITIVE_INFINITY, result)
    }

    @Test
    fun `test missing closing parentheses`() {
        assertThrows<ParseException> {
            Evaluator.compile("2 + (3 * 4", defaultEnv)
        }
    }

    @Test
    fun `test invalid character input`() {
        assertThrows<TokenizerException> {
            Evaluator.compile("2 + @", defaultEnv)
        }
    }

    @Test
    fun `test function with too many arguments`() {
        val ex = assertThrows<RuntimeException> {
            Evaluator.compile("sqrt(1, 2)", defaultEnv)
        }
        assertEquals("sqrt expects one number", ex.message)
    }

    @Test
    fun `test nested expression with whitespace`() {
        val result = Evaluator.compile("\" 3 + 4 * 2 \"", defaultEnv)
        // Expected: 3 + (4 * 2) = 11
        assertEquals(11.0, result)
    }

    @Test
    fun `test complex combined expression`() {
        val result = Evaluator.compile("sqrt(16) + sin(pi/2) * (2 ^ 3 - 1)", defaultEnv)
        assertEquals(11.0, result)
    }

    @Test
    fun `test explicit nested function call using implicit syntax`() {
        val result = Evaluator.compile("sin(cos 0)", defaultEnv)
        // cos(0) = 1, sin(1) ≈ 0.84147
        assertEquals(Math.sin(1.0), result, 0.0001)
    }

    @Test
    fun `test missing implicit argument for binary function`() {
        val ex = assertThrows<RuntimeException> {
            Evaluator.compile("max 1", defaultEnv)
        }
        assertEquals("max expects two numbers", ex.message)
    }

    @Test
    fun `test custom function with one variable using environment`() {
        val env = Environment.default()
        env.define("f", FunctionValue(1) { args ->
            args[0] * 3
        })
        val resultExplicit = Evaluator.compile("f(4)", env)
        assertEquals(12.0, resultExplicit)
        val resultImplicit = Evaluator.compile("f 4", env)
        assertEquals(12.0, resultImplicit)
    }

    @Test
    fun `test custom function with two variables using environment`() {
        val env = Environment.default()
        env.define("g", FunctionValue(2) { args ->
            args[0] + args[1]
        })
        val result = Evaluator.compile("g(7, 8)", env)
        assertEquals(15.0, result)
    }

    @Test
    fun `test custom function with three variables using environment`() {
        val env = Environment.default()
        env.define("h", FunctionValue(3) { args ->
            args[0] - args[1] * args[2]
        })
        val result = Evaluator.compile("h(10, 2, 3)", env)
        assertEquals(4.0, result)
    }

    @Test
    fun `test function using external environment variable`() {
        val parentEnv = Environment.default()
        parentEnv.define("a", NumberValue(5.0))
        val env = Environment().addParent(parentEnv)
        env.define("f", FunctionValue(1) { args ->
            args[0] + (env.lookup("a") as NumberValue).value
        })
        val result = Evaluator.compile("f(10)", env)
        assertEquals(15.0, result)
    }

    @Test
    fun `test expression with environment variables`() {
        val env = Environment.default()
        // Set x = 3 and y = 16
        env.define("x", NumberValue(3.0))
        env.define("y", NumberValue(16.0))
        // (3+5)^2 - sqrt(16) = 8^2 - 4 = 64 - 4 = 60
        val result = Evaluator.compile("(x+5)^2 - sqrt(y)", env)
        assertEquals(60.0, result)
    }

    @Test
    fun `test expression with sqrt negative edge case`() {
        val env = Environment.default()
        // Set x = 3 and y = -4 (sqrt(-4) equals NaN)
        env.define("x", NumberValue(3.0))
        env.define("y", NumberValue(-4.0))
        val result = Evaluator.compile("(x+5)^2 - sqrt(y)", env)
        assertTrue(result.isNaN())
    }

    @Test
    fun `test expression with zero values`() {
        val env = Environment.default()
        // Set x = 0 and y = 0
        env.define("x", NumberValue(0.0))
        env.define("y", NumberValue(0.0))
        // (0+5)^2 - sqrt(0) = 25 - 0 = 25
        val result = Evaluator.compile("(x+5)^2 - sqrt(y)", env)
        assertEquals(25.0, result)
    }

    @Test
    fun `test expression with non-integer values`() {
        val env = Environment.default()
        // Set x = 2.5 and y = 2.25
        env.define("x", NumberValue(2.5))
        env.define("y", NumberValue(2.25))
        // (2.5+5)^2 - sqrt(2.25) = (7.5)^2 - 1.5 = 56.25 - 1.5 = 54.75
        val result = Evaluator.compile("(x+5)^2 - sqrt(y)", env)
        assertEquals(54.75, result, 0.0001)
    }

    @Test
    fun `test missing environment variable`() {
        val env = Environment.default()
        // Only y is defined, x missing
        env.define("y", NumberValue(16.0))
        val ex = assertThrows<RuntimeException> {
            Evaluator.compile("(x+5)^2 - sqrt(y)", env)
        }
        assertEquals("Unknown identifier \"x\"", ex.message)
    }

}
