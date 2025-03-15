package org.example

import kotlin.math.*

/**
 * Base class for values in the evaluator.
 */
sealed class Value

/**
 * Represents a numeric value.
 *
 * @property value the double value
 */
data class NumberValue(val value: Double) : Value()

/**
 * Represents a function value.
 *
 * @property parameters the number of expected arguments
 * @property implementation a lambda implementing the function logic
 */
data class FunctionValue(
    val parameters: Int,
    val implementation: (List<Double>) -> Double
) : Value()

/**
 * Represents an evaluation environment for variables and functions.
 *
 * Each environment can have an optional parent for nested lookups.
 */
class Environment(
    private var parent: Environment? = null,
) {
    private val fields: MutableMap<String, Value> = mutableMapOf()

    /**
     * Adds or replaces the parent environment.
     *
     * @param parent the parent environment to use for lookups
     * @return this environment
     */
    fun addParent(parent: Environment?): Environment {
        this.parent = parent
        return this
    }

    /**
     * Looks up a value by its identifier.
     *
     * @param name the variable or function name
     * @return the associated value
     * @throws RuntimeException if the identifier is unknown
     */
    fun lookup(name: String): Value {
        return fields[name] ?: parent?.lookup(name)
        ?: throw RuntimeException("Unknown identifier \"$name\"")
    }

    /**
     * Defines a new variable or function.
     *
     * @param name the identifier
     * @param value the value to associate
     */
    fun define(name: String, value: Value) {
        fields[name] = value
    }

    companion object {
        /**
         * Returns a default environment with predefined constants and functions.
         *
         * Constants: pi, e.
         * One-argument functions: sin, cos, tan, sqrt, abs, exp, round, floor, ceil.
         * Two-argument functions: log, max, min.
         *
         * @return the default environment
         */
        fun default(): Environment {
            val environment = Environment()

            // Constants
            environment.define("pi", NumberValue(PI))
            environment.define("e", NumberValue(E))

            // 1-Argument functions
            listOf(
                "sin" to ::sin,
                "cos" to ::cos,
                "tan" to ::tan,
                "sqrt" to ::sqrt,
                "abs" to { a: Double -> abs(a) },
                "exp" to ::exp,
                "round" to ::round,
                "floor" to ::floor,
                "ceil" to ::ceil
            ).forEach { (name, func) ->
                environment.define(name, FunctionValue(1) { args ->
                    if (args.size != 1) throw RuntimeException("$name expects one number")
                    func(args[0])
                })
            }

            // Two-Argument functions
            listOf(
                "log" to { a: Double, b: Double -> log(a, b) },
                "max" to { a: Double, b: Double -> max(a, b) },
                "min" to { a: Double, b: Double -> min(a, b) },
            ).forEach { (name, func) ->
                environment.define(name, FunctionValue(2) { args ->
                    if (args.size != 2) {
                        throw RuntimeException("$name expects two numbers")
                    }
                    func(args[0], args[1])
                })
            }

            return environment
        }
    }
}
