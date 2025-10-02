package com.example.scientificcalculator

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    lateinit var tvMain: TextView
    lateinit var tvSec: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvMain = findViewById(R.id.tvmain)
        tvSec = findViewById(R.id.tvsec)

        val numbers = listOf(
            R.id.b0, R.id.b1, R.id.b2, R.id.b3, R.id.b4,
            R.id.b5, R.id.b6, R.id.b7, R.id.b8, R.id.b9, R.id.bdot
        )
        numbers.forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                appendToMain((it as Button).text.toString())
            }
        }

        findViewById<Button>(R.id.bplus).setOnClickListener { appendToMain("+") }
        findViewById<Button>(R.id.bminus).setOnClickListener { appendToMain("-") }
        findViewById<Button>(R.id.bmul).setOnClickListener { appendToMain("*") }
        findViewById<Button>(R.id.bdiv).setOnClickListener { appendToMain("/") }

        findViewById<Button>(R.id.bac).setOnClickListener {
            tvMain.text = "0"
            tvSec.text = ""
        }
        findViewById<Button>(R.id.bc).setOnClickListener {
            val str = tvMain.text.toString()
            if (str.isNotEmpty()) tvMain.text = str.dropLast(1)
        }
        findViewById<Button>(R.id.bbrac1).setOnClickListener { appendToMain("(") }
        findViewById<Button>(R.id.bbrac2).setOnClickListener { appendToMain(")") }
        findViewById<Button>(R.id.bpi).setOnClickListener { appendToMain(Math.PI.toString()) }
        findViewById<Button>(R.id.bpow).setOnClickListener { appendToMain("^") }

        findViewById<Button>(R.id.bsin).setOnClickListener { appendToMain("sin(") }
        findViewById<Button>(R.id.bcos).setOnClickListener { appendToMain("cos(") }
        findViewById<Button>(R.id.btan).setOnClickListener { appendToMain("tan(") }
        findViewById<Button>(R.id.basin).setOnClickListener { appendToMain("asin(") }
        findViewById<Button>(R.id.bacos).setOnClickListener { appendToMain("acos(") }
        findViewById<Button>(R.id.batan).setOnClickListener { appendToMain("atan(") }
        findViewById<Button>(R.id.blog).setOnClickListener { appendToMain("log(") }
        findViewById<Button>(R.id.bln).setOnClickListener { appendToMain("ln(") }
        findViewById<Button>(R.id.bsqrt).setOnClickListener { appendToMain("sqrt(") }
        findViewById<Button>(R.id.bsquare).setOnClickListener { appendToMain("^2") }
        findViewById<Button>(R.id.binv).setOnClickListener { appendToMain("1/") }
        findViewById<Button>(R.id.bfact).setOnClickListener { appendToMain("fact(") }

        findViewById<Button>(R.id.bequal).setOnClickListener {
            try {
                val expr = tvMain.text.toString()
                val result = evaluate(expr)
                tvSec.text = expr
                tvMain.text = result.toString()
            } catch (_: Exception) {
                tvMain.text = "Error"
            }
        }
    }

    private fun appendToMain(str: String) {
        if (tvMain.text.toString() == "0") tvMain.text = ""
        tvMain.append(str)
    }

    private fun evaluate(expr: String): Double {
        var expression = expr
            .replace("÷", "/")
            .replace("×", "*")
            .replace("π", Math.PI.toString())

        while (expression.contains("fact(")) {
            val start = expression.indexOf("fact(") + 5
            val end = findClosingBracket(expression, start - 1)
            val inside = expression.substring(start, end)
            val value = factorial(evaluate(inside).toInt())
            expression = expression.replaceRange(start - 5, end + 1, value.toString())
        }

        return object : Any() {
            var pos = -1
            var ch = 0
            fun nextChar() { ch = if (++pos < expression.length) expression[pos].code else -1 }
            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) { nextChar(); return true }
                return false
            }
            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) throw RuntimeException("Unexpected: " + expression[pos])
                return x
            }
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    when {
                        eat('+'.code) -> x += parseTerm()
                        eat('-'.code) -> x -= parseTerm()
                        else -> return x
                    }
                }
            }
            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    when {
                        eat('*'.code) -> x *= parseFactor()
                        eat('/'.code) -> x /= parseFactor()
                        else -> return x
                    }
                }
            }
            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()

                var x: Double
                val startPos = pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
                    while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
                    x = expression.substring(startPos, pos).toDouble()
                } else if (ch in 'a'.code..'z'.code) {
                    while (ch in 'a'.code..'z'.code) nextChar()
                    val func = expression.substring(startPos, pos)
                    x = parseFactor()
                    x = when (func) {
                        "sqrt" -> sqrt(x)
                        "sin" -> sin(Math.toRadians(x))
                        "cos" -> cos(Math.toRadians(x))
                        "tan" -> tan(Math.toRadians(x))
                        "asin" -> Math.toDegrees(asin(x))
                        "acos" -> Math.toDegrees(acos(x))
                        "atan" -> Math.toDegrees(atan(x))
                        "log" -> log10(x)
                        "ln" -> ln(x)
                        else -> throw RuntimeException("Unknown func: $func")
                    }
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }

                if (eat('^'.code)) x = x.pow(parseFactor())
                return x
            }
        }.parse()
    }

    private fun factorial(n: Int): Double {
        var result = 1.0
        for (i in 2..n) result *= i
        return result
    }

    private fun findClosingBracket(expr: String, start: Int): Int {
        var count = 0
        for (i in start until expr.length) {
            if (expr[i] == '(') count++
            if (expr[i] == ')') count--
            if (count == 0) return i
        }
        return -1
    }
}
