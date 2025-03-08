package jsondatabase.database

import kotlinx.serialization.json.*

class CommandParser {
    fun parse(input: String): Result<DatabaseCommand> {
        return try {
            val request = Request.fromJson(input)
            when (request.type.lowercase()) {
                "get" -> parseGet(request)
                "set" -> parseSet(request)
                "delete" -> parseDelete(request)
                "exit" -> Result.success(DatabaseCommand.Exit)
                else -> Result.failure(IllegalArgumentException("Unknown command"))
            }
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Invalid JSON format"))
        }
    }

    private fun parseGet(request: Request): Result<DatabaseCommand.Get> {
        return when {
            request.key == null -> Result.failure(IllegalArgumentException("Key is required for get command"))
            else -> Result.success(DatabaseCommand.Get(parseKey(request.key)))
        }
    }

    private fun parseSet(request: Request): Result<DatabaseCommand.Set> {
        return when {
            request.key == null -> Result.failure(IllegalArgumentException("Key is required for set command"))
            request.value == null -> Result.failure(IllegalArgumentException("Value is required for set command"))
            else -> Result.success(DatabaseCommand.Set(parseKey(request.key), request.value))
        }
    }

    private fun parseDelete(request: Request): Result<DatabaseCommand.Delete> {
        return when {
            request.key == null -> Result.failure(IllegalArgumentException("Key is required for delete command"))
            else -> Result.success(DatabaseCommand.Delete(parseKey(request.key)))
        }
    }

    private fun parseKey(key: JsonElement): List<String> {
        return when (key) {
            is JsonArray -> key.map { it.jsonPrimitive.content }
            is JsonPrimitive -> listOf(key.content)
            else -> throw IllegalArgumentException("Key must be a string or an array of strings")
        }
    }
}
