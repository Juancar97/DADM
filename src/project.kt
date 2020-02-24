import java.io.File
import java.io.PrintWriter
import kotlin.math.max
import java.util.*
import kotlin.math.roundToInt

open class Card(var question: String, var answer: String, val id: String, val date: String) {
    constructor(question: String, answer: String) : this(question, answer, id = UUID.randomUUID().toString(), date = Date().toString())
    var quality = 0
    var repetitions = 0
    var interval = 1
    var nextPracticeDate = 0
    var easiness = 2.5
    var currentDate = 0
    var prueba = 5

    companion object {
        fun leer() : Card {
            print("Teclea el tipo 0 (Card) 1 (Cloze): ")
            val option = readLine()!!.toInt()
            if(option == 0) {
                print("   Teclea la pregunta: ")
                val question = readLine()!!
                print("   Teclea la respuesta: ")
                val answer = readLine()!!
                return Card(question, answer)
            }
            else {
                print("   Teclea la pregunta: ")
                val question = readLine()!!
                print("   Teclea la respuesta: ")
                val answer = readLine()!!
                return Cloze(question, answer)
            }
        }
    }

    open fun show() {
        println(question)
        print("INTRO para ver respuesta: ")
        val enter = readLine()!!
        println(answer)
        print("Teclea 0 (Difícil) 3 (Dudo) 5 (Fácil): ")
        quality = readLine()!!.toInt()
    }

    fun update() {
        easiness = max(easiness + 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02), 1.3)
        if(quality < 3)
            repetitions = 0
        else
            repetitions++

        interval = when(repetitions) {
            0, 1 -> 1
            2    -> 6
            else -> (interval * easiness).roundToInt()
        }
        nextPracticeDate = (currentDate + interval)
    }

    fun details() {
        println("   $question ($answer) eas = ${"%.1f".format(easiness)} rep = $repetitions int = $interval next = $nextPracticeDate")
    }
}

class Cloze(question: String, answer: String, id: String, date: String) : Card(question, answer, id, date) {
    constructor(question: String, answer: String) : this(question, answer, id = UUID.randomUUID().toString(), date = Date().toString())

    override fun show() {
        println(question)
        print("INTRO para ver respuesta: ")
        val enter = readLine()!!
        var sentence = question.replace(question.substringBeforeLast("*"), answer)
        sentence = sentence.replace("*", "")
        println(sentence)
        print("Teclea 0 (Difícil) 3 (Dudo) 5 (Fácil): ")
        quality = readLine()!!.toInt()
    }
}

fun leerTarjetas(cards: MutableList<Card>) {
    val lineas: List<String> = File("data/tarjetas.txt").readLines()
    var trozos: List<String>
    var instance: Int
    var question: String
    var answer: String
    var id: String
    var date: String
    var card: Card
    var cont = 0

    cards.clear()
    for (i in lineas) {
        trozos = i.split("|")
        instance = trozos.get(0).toInt()
        question = trozos.get(1)
        answer = trozos.get(2)
        id = trozos.get(3)
        date = trozos.get(4)

        if (instance == 0)
            card = Card(question, answer, id, date)
        else
            card = Cloze(question, answer, id, date)

        card.quality = trozos.get(5).toInt()
        card.repetitions = trozos.get(6).toInt()
        card.interval = trozos.get(7).toInt()
        card.nextPracticeDate = trozos.get(8).toInt()
        card.easiness = trozos.get(9).toDouble()
        card.currentDate = trozos.get(10).toInt()
        cards.add(card)
        cont++
    }
    println("Se han leido $cont tarjetas")
}

fun guardarTarjetas(cards: MutableList<Card>, writer: PrintWriter) {
    var cont = 0
    if (cards.isEmpty())
        println("No hay tarjetas que guardar")
    else {
        for (tarjeta in cards) {
            if (tarjeta is Cloze) {
                writer.appendln(
                    "1|${tarjeta.question}|${tarjeta.answer}|${tarjeta.id}|${tarjeta.date}|" +
                            "${tarjeta.quality}|${tarjeta.repetitions}|${tarjeta.interval}|${tarjeta.nextPracticeDate}|" +
                            "${tarjeta.easiness}|${tarjeta.currentDate}"
                )
            } else {
                writer.appendln(
                    "0|${tarjeta.question}|${tarjeta.answer}|${tarjeta.id}|${tarjeta.date}|" +
                            "${tarjeta.quality}|${tarjeta.repetitions}|${tarjeta.interval}|${tarjeta.nextPracticeDate}|" +
                            "${tarjeta.easiness}|${tarjeta.currentDate}"
                )
            }
            cont++
        }
        writer.close()
        cards.clear()
    }
    println("Se han guardado $cont tarjetas")
}

fun simulate(cards: MutableList<Card>, dias: Int) {
    var i = 0
    cards[0].quality = 5
    cards[1].quality = 3
    cards[2].quality = 0

    while(i < dias) {
        println("Fecha: $i")
        cards[0].update()
        cards[1].update()
        cards[2].update()
        cards[0].details()
        cards[1].details()
        cards[2].details()
        cards[0].currentDate++
        cards[1].currentDate++
        cards[2].currentDate++
        i++
    }
}

fun main() {
    val cards = mutableListOf<Card>()

    leerTarjetas(cards)

    do {
        println("")
        println("1. Añadir tarjeta")
        println("2. Presentar tarjetas")
        println("3. Leer tarjetas de fichero")
        println("4. Escribir tarjetas en fichero")
        println("5. Simulación")
        println("6. Salir")
        print("Elige una opción: ")
        var opcion = readLine()!!.toInt()
        println("")

        if (opcion == 1)
            cards.add(Card.leer())
        else if (opcion == 2)
            if(cards.isEmpty())
                println("No hay tarjetas que mostrar")
            else
                cards.forEach { it.show() }
        else if (opcion == 3) {
            leerTarjetas(cards)
        } else if (opcion == 4) {
            guardarTarjetas(cards, PrintWriter("data/tarjetas.txt"))
        } else if (opcion == 5) {
            if (cards.isEmpty())
                println("No es posible hacer una simulacion")
            else {
                print("Introduce un numero de dias para la simulacion: ")
                val dias = readLine()!!.toInt()
                val tarjetas = mutableListOf(Card("Facil", "Easy"))
                tarjetas.add(Card("Doubt", "Duda"))
                tarjetas.add(Card("Dificil", "Difficult"))
                simulate(tarjetas, dias)
                println("La simulacion ha terminado")
            }
        }
    } while(opcion in 1..5)

    guardarTarjetas(cards, PrintWriter("data/tarjetas.txt"))
}