package jsondatabase.database

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

@Serializable
data class Request(
    val type: String,
    val key: JsonElement? = null,
    val value: JsonElement? = null
) {
    companion object {
        fun fromJson(json: String): Request = Json.decodeFromString<Request>(json)
    }

    fun toJson(): String = Json.encodeToString(this)
}

@Serializable
data class Response(
    val response: String,
    val value: JsonElement? = null,
    val reason: String? = null
) {
    companion object {
        fun ok(value: JsonElement? = null) = Response("OK", value = value)
        fun error(reason: String) = Response("ERROR", reason = reason)
        fun fromJson(json: String): Response = Json.decodeFromString<Response>(json)
    }

    fun toJson(): String = Json.encodeToString(this)
}
