package jsondatabase.server

import jsondatabase.database.CommandParser
import jsondatabase.database.DatabaseService
import jsondatabase.database.FileDatabase
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.util.concurrent.Executors

fun main() {
    val address = "127.0.0.1"
    val port = 23456

    println("Server started!")

    val server = ServerSocket(port, 50, InetAddress.getByName(address))
    val database = FileDatabase()
    val commandParser = CommandParser()
    val databaseService = DatabaseService(database, commandParser)

    // Create a thread pool for handling client requests
    val executor = Executors.newFixedThreadPool(4)

    var running = true
    while (running) {
        val socket = server.accept()

        // Submit client handling task to executor
        executor.submit {
            try {
                val input = DataInputStream(socket.getInputStream())
                val output = DataOutputStream(socket.getOutputStream())

                val message = input.readUTF()
                println("Received: $message")

                val (continueRunning, response) = databaseService.processCommand(message)

                // Synchronize access to the running flag
                synchronized(server) {
                    if (!continueRunning) {
                        running = false
                    }
                }

                output.writeUTF(response)
                socket.close()

                // If exit command received, shut down the server
                if (!continueRunning) {
                    synchronized(server) {
                        if (!server.isClosed) {
                            server.close()
                            executor.shutdown()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Wait for all tasks to complete before exiting
    executor.shutdown()
}
