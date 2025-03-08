package jsondatabase.database

import kotlinx.serialization.json.*

interface Database {
    fun get(key: List<String>): Result<JsonElement>
    fun set(key: List<String>, value: JsonElement): Result<Unit>
    fun delete(key: List<String>): Result<Unit>
}
