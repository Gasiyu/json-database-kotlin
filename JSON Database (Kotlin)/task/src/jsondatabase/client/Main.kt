package jsondatabase.client

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.net.InetAddress
import java.net.Socket
import jsondatabase.database.Request
import kotlinx.serialization.json.*

fun main(args: Array<String>) {
    println("Client started!")

    val address = "127.0.0.1"
    val port = 23456

    // Parse command line arguments
    var type = ""
    var key = ""
    var value = ""
    var inputFile: String? = null

    var i = 0
    while (i < args.size) {
        when (args[i]) {
            "-t" -> {
                if (i + 1 < args.size) type = args[i + 1]
                i += 2
            }
            "-k" -> {
                if (i + 1 < args.size) key = args[i + 1]
                i += 2
            }
            "-v" -> {
                if (i + 1 < args.size) value = args[i + 1]
                i += 2
            }
            "-in" -> {
                if (i + 1 < args.size) inputFile = args[i + 1]
                i += 2
            }
            else -> i++
        }
    }

    // Determine the JSON request
    val jsonRequest = if (inputFile != null) {
        // Construct the path to the client data file
        val userDir = System.getProperty("user.dir")
        val clientDataPath = "$userDir${File.separator}src${File.separator}jsondatabase${File.separator}client${File.separator}data"

        // Read request from file
        val file = File("$clientDataPath${File.separator}$inputFile")
        if (!file.exists()) {
            throw IllegalArgumentException("Input file not found: $inputFile")
        }
        file.readText()
    } else {
        // Create request from command line arguments
        val request = when (type) {
            "exit" -> Request(type = "exit")
            "get" -> Request(type = "get", key = JsonPrimitive(key))
            "delete" -> Request(type = "delete", key = JsonPrimitive(key))
            "set" -> Request(type = "set", key = JsonPrimitive(key), value = JsonPrimitive(value))
            else -> throw IllegalArgumentException("Unknown command type: $type")
        }
        request.toJson()
    }

    val socket = Socket(InetAddress.getByName(address), port)
    val input = DataInputStream(socket.getInputStream())
    val output = DataOutputStream(socket.getOutputStream())

    println("Sent: $jsonRequest")
    output.writeUTF(jsonRequest)

    val response = input.readUTF()
    println("Received: $response")

    socket.close()
}
