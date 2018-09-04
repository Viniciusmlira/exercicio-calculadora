package br.ufpe.cin.if710.calculadora

import android.app.Activity
import android.os.Bundle
import android.util.*
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    //detecta-se o valor do botão clicado através do campo "text" do TextView de cada Button e exibe na tela
    //o resultado da ação. Pode ser apenas um valor ou o resultado do cálculo da expressão.
    fun handleClick(v: View) {
        var campoTexto = findViewById(R.id.text_calc) as TextView
        var texto: String = campoTexto.text.toString()
        var valorBotao = (v as Button).getText().toString()
        Log.i("num", valorBotao)

        var expressao = texto + valorBotao;

        //compara se o valor chamado é a avaliação da expressão com o sinal de "=" e
        //verifica se a expressão é válida. Se for válida, mostra o resultado dela
        //se for inválida, exibe uma exceção
        if (valorBotao == "=") {

            try {
                var resultado = eval(texto).toString();
                var campoResultado = findViewById(R.id.text_info) as TextView
                campoResultado.setText(resultado)
            } catch (e: Exception) {
                Toast.makeText(this, "Operação inválida. Tente novamente", Toast.LENGTH_SHORT).show()
            }

            //limpamos os valores digitados, para iniciar uma nova expressão
        } else if (valorBotao == "C") {
            campoTexto.setText("")
        } else {
            //caso não seja avaliação nem limpeza da expressão, continuamos adicionando símbolos
            campoTexto.setText(expressao)
        }

    }


    //Como usar a função:
    // eval("2+2") == 4.0
    // eval("2+3*4") = 14.0
    // eval("(2+3)*4") = 20.0
    //Fonte: https://stackoverflow.com/a/26227947
    fun eval(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch: Char = ' '
            fun nextChar() {
                val size = str.length
                ch = if ((++pos < size)) str.get(pos) else (-1).toChar()
            }

            fun eat(charToEat: Char): Boolean {
                while (ch == ' ') nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Caractere inesperado: " + ch)
                return x
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            // | number | functionName factor | factor `^` factor
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'))
                        x += parseTerm() // adição
                    else if (eat('-'))
                        x -= parseTerm() // subtração
                    else
                        return x
                }
            }



            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'))
                        x *= parseFactor() // multiplicação
                    else if (eat('/'))
                        x /= parseFactor() // divisão
                    else
                        return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+')) return parseFactor() // + unário
                if (eat('-')) return -parseFactor() // - unário
                var x: Double
                val startPos = this.pos
                if (eat('(')) { // parênteses
                    x = parseExpression()
                    eat(')')
                } else if ((ch in '0'..'9') || ch == '.') { // números
                    while ((ch in '0'..'9') || ch == '.') nextChar()
                    x = java.lang.Double.parseDouble(str.substring(startPos, this.pos))
                } else if (ch in 'a'..'z') { // funções
                    while (ch in 'a'..'z') nextChar()
                    val func = str.substring(startPos, this.pos)
                    x = parseFactor()
                    if (func == "sqrt")
                        x = Math.sqrt(x)
                    else if (func == "sin")
                        x = Math.sin(Math.toRadians(x))
                    else if (func == "cos")
                        x = Math.cos(Math.toRadians(x))
                    else if (func == "tan")
                        x = Math.tan(Math.toRadians(x))
                    else
                        throw RuntimeException("Função desconhecida: " + func)
                } else {
                    throw RuntimeException("Caractere inesperado: " + ch.toChar())
                }
                if (eat('^')) x = Math.pow(x, parseFactor()) // potência
                return x
            }
        }.parse()
    }
}
