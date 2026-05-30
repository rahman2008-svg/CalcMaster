package com.example.util

import kotlin.math.*

object MathEvaluator {
    fun evaluate(expression: String): Double {
        val clean = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", "3.141592653589793")
            .replace("e", "2.718281828459045")
            .replace(" ", "")
        
        return Parser(clean).parse()
    }

    private class Parser(val str: String) {
        var pos = -1
        var ch = 0

        fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw RuntimeException("Unexpected character: " + ch.toChar())
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm()
                else if (eat('-'.code)) x -= parseTerm()
                else return x
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code)) x *= parseFactor()
                else if (eat('/'.code)) {
                    val divisor = parseFactor()
                    if (divisor == 0.0) throw ArithmeticException("Division by zero")
                    x /= divisor
                } else return x
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = this.pos
            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) {
                while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                x = str.substring(startPos, this.pos).toDouble()
            } else if (ch >= 'a'.code && ch <= 'z'.code) {
                while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                val func = str.substring(startPos, this.pos)
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else {
                    x = parseFactor()
                }
                x = when (func) {
                    "sqrt" -> sqrt(x)
                    "sin" -> sin(Math.toRadians(x))
                    "cos" -> cos(Math.toRadians(x))
                    "tan" -> tan(Math.toRadians(x))
                    "log" -> log10(x)
                    "ln" -> ln(x)
                    else -> throw RuntimeException("Unknown function: $func")
                }
            } else {
                throw RuntimeException("Unexpected character: " + ch.toChar())
            }

            if (eat('^'.code)) x = x.pow(parseFactor())

            return x
        }
    }
}
