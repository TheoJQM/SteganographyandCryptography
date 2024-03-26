package cryptography

import java.awt.Color
import java.io.File
import javax.imageio.ImageIO

const val endMessage = "000000000000000000000011"

class StegCryptoEngine {
    private lateinit var inputFile: File
    private lateinit var outputFile: File
    private lateinit var message: String
    private lateinit var passWord: String

    fun work() {
        while (true) {
            println("Task (hide, show, exit): ")
            when (val input = readln()) {
                "exit" -> {
                    println("Bye!")
                    break
                }
                "hide" -> hide()
                "show" -> show()
                else -> println("Wrong task: $input")
            }
        }
    }

    private fun hide(){
        println("Input image file: ")
        inputFile = File(readln())
        println("Output image file: ")
        outputFile = File(readln())
        println("Message to hide: ")
        message = transformMessageToBits(readln())
        println("Password: ")
        passWord = transformMessageToBits(readln())


        if (!checkInput()) return
        val image = ImageIO.read(inputFile)
        if (image.width * image.height < message.length) {
            println("The input image is not large enough to hold this message.")
            return
        }

        encodeMessage(message)
        message += endMessage
        for (j in 0 until image.height) {
            for (i in 0 until image.width) {
                if (message.isEmpty()) break
                val pixel = Color(image.getRGB(i, j))
                val redValue = pixel.red
                val greenValue = pixel.green
                val blueValue = hideBit(pixel.blue)
                image.setRGB(i, j, Color(redValue, greenValue, blueValue).rgb)
            }
        }
        ImageIO.write(image, "png", outputFile)
        println("Message saved in ${outputFile.path.replace('\\', '/')} image.")
    }

    private fun checkInput(): Boolean {
        return if (!inputFile.exists()) {
            println("Can't read input file!")
            false
        } else {
            true
        }
    }

    private fun transformMessageToBits(message: String): String {
        return message.encodeToByteArray().joinToString("") { it.toInt().toString(2).padStart(8, '0') }
    }

    private fun encodeMessage(newMessage: String) {
        var encodedMessage = ""
        var counter = 0
        for (i in newMessage.indices) {
            if (i >= (counter + 1) * passWord.length) counter++
            encodedMessage += newMessage[i].toString().toInt() xor passWord[i - counter * passWord.length].toString().toInt()
        }
        message = encodedMessage
    }

    private fun hideBit(color: Int): Int {
        val bitToHide = message.first().toString().toInt()
        message = message.removeRange(0, 1)
        return color.and(254).or(bitToHide) % 256
    }

    private fun show() {
        println("Input image file:")
        inputFile = File(readln())
        println("Password: ")
        passWord = transformMessageToBits(readln())
        val resultMessage = getHiddenMessage()
        if (resultMessage != "") {
            encodeMessage(resultMessage)
            println("Message:")
            val byteArray = message.chunked(8) { it.toString().toInt(2).toByte() }.toByteArray()
            println(byteArray.toString(Charsets.UTF_8))
        }
    }

    private fun getHiddenMessage(): String {
        if (!checkInput()) return ""
        val image = ImageIO.read(inputFile)
        var bitMessage = ""
        var byteArray = byteArrayOf()

        for (j in 0 until image.height) {
            for (i in 0 until image.width) {
                if (bitMessage.length > endMessage.length && bitMessage.endsWith(endMessage)) break
                bitMessage += Color(image.getRGB(i, j)).blue and 1
            }
        }

        while (bitMessage.isNotEmpty()) {
            val bit = bitMessage.substring(0, 8)
            val byte = Integer.parseInt(bit, 2).toByte()
            byteArray = byteArray.plus(byte)
            bitMessage =  bitMessage.removeRange(0, 8)
        }
        val encodedMessage = byteArray.toString(Charsets.UTF_8)
        return transformMessageToBits(encodedMessage).removeSuffix(endMessage)
    }
}

fun main() {
    val tool = StegCryptoEngine()
    tool.work()
}

