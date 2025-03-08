package jsondatabase.database

import kotlinx.serialization.json.*

class DatabaseService(
    private val database: Database,
    private val commandParser: CommandParser
) {
    fun processCommand(input: String): Pair<Boolean, String> {
        val command = commandParser.parse(input).getOrElse {
            return Pair(true, Response.error("Invalid command format").toJson())
        }

        val response = when (command) {
            is DatabaseCommand.Get -> {
                database.get(command.key)
                    .fold(
                        onSuccess = { Response.ok(it) },
                        onFailure = { Response.error("No such key") }
                    )
            }
            is DatabaseCommand.Set -> {
                database.set(command.key, command.value)
                    .fold(
                        onSuccess = { Response.ok() },
                        onFailure = { Response.error("Error setting value") }
                    )
            }
            is DatabaseCommand.Delete -> {
                database.delete(command.key)
                    .fold(
                        onSuccess = { Response.ok() },
                        onFailure = { Response.error("No such key") }
                    )
            }
            DatabaseCommand.Exit -> Response.ok()
        }

        return Pair(command !is DatabaseCommand.Exit, response.toJson())
    }
}
