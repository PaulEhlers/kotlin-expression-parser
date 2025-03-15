package org.example

import kotlin.math.*

sealed class Value

data class NumberValue(val value: Double) : Value()

data class FunctionValue(
    val parameters: Int,
    val implementation: (List<Double>) -> Double
) : Value()

class Environment (
    private var parent: Environment? = null,
) {
    private val fields: MutableMap<String, Value> = mutableMapOf()

    fun addParent(parent: Environment?) : Environment {
        this.parent = parent
        return this
    }

    fun lookup(name: String): Value {
        return fields[name] ?: parent?.lookup(name)
        ?: throw RuntimeException("Unknown identifier \"$name\"")
    }

    fun define(name: String, value: Value) {
        fields[name] = value
    }
    companion object {
        fun default(): Environment {
            val environment = Environment()

            // Konstanten
            environment.define("pi", NumberValue(PI))
            environment.define("e", NumberValue(E))

            // Einfache 1-Argument Funktionen
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
                    func((args[0] as Double))
                })
            }

            // Zwei-Argument Funktionen
            listOf(
                "log" to { a: Double, b: Double -> log(a, b) },
                "max" to { a: Double, b: Double -> max(a, b) },
                "min" to { a: Double, b: Double -> min(a, b) },
            ).forEach { (name, func) ->
                environment.define(name, FunctionValue(2) { args ->
                    if (args.size != 2) {
                        throw RuntimeException("$name expects two numbers")
                    }
                    func((args[0] as Double), args[1] as Double)
                })
            }

            return environment
        }
    }
}