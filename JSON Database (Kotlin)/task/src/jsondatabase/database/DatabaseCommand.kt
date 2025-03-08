package jsondatabase.database

import kotlinx.serialization.json.*

sealed interface DatabaseCommand {
    data class Get(val key: List<String>) : DatabaseCommand
    data class Set(val key: List<String>, val value: JsonElement) : DatabaseCommand
    data class Delete(val key: List<String>) : DatabaseCommand
    object Exit : DatabaseCommand
}
