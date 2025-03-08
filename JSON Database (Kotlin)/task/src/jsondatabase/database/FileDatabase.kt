package jsondatabase.database

import kotlinx.serialization.json.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

class FileDatabase : Database {
    private val dbFile: File
    private val lock: ReadWriteLock = ReentrantReadWriteLock()
    private val readLock = lock.readLock()
    private val writeLock = lock.writeLock()

    init {
        val dbPath = "JSON Database (Kotlin)${File.separator}task${File.separator}src${File.separator}jsondatabase${File.separator}server${File.separator}data"
        dbFile = File("$dbPath${File.separator}db.json")

        // Create directory if it doesn't exist
        val dir = File(dbPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        // Create file if it doesn't exist
        if (!dbFile.exists()) {
            dbFile.createNewFile()
            // Initialize with empty JSON object
            writeLock.lock()
            try {
                dbFile.writeText("{}")
            } finally {
                writeLock.unlock()
            }
        }
    }

    private fun readDatabase(): Map<String, JsonElement> {
        readLock.lock()
        return try {
            val content = dbFile.readText()
            if (content.isBlank()) {
                emptyMap()
            } else {
                Json.decodeFromString<JsonObject>(content).toMap()
            }
        } catch (e: Exception) {
            emptyMap()
        } finally {
            readLock.unlock()
        }
    }

    private fun writeDatabase(data: Map<String, JsonElement>) {
        writeLock.lock()
        try {
            val jsonObject = JsonObject(data.toMutableMap())
            val json = Json.encodeToString(jsonObject)
            dbFile.writeText(json)
        } finally {
            writeLock.unlock()
        }
    }

    override fun get(key: List<String>): Result<JsonElement> {
        if (key.isEmpty()) {
            return Result.failure(IllegalStateException("Key cannot be empty"))
        }

        val data = readDatabase()
        val rootKey = key.first()
        val rootValue = data[rootKey]

        if (rootValue == null) {
            return Result.failure(IllegalStateException("No such key"))
        }

        if (key.size == 1) {
            return Result.success(rootValue)
        }

        return getNestedValue(rootValue, key.drop(1))
    }

    override fun set(key: List<String>, value: JsonElement): Result<Unit> {
        if (key.isEmpty()) {
            return Result.failure(IllegalStateException("Key cannot be empty"))
        }

        val data = readDatabase().toMutableMap()
        val rootKey = key.first()

        if (key.size == 1) {
            data[rootKey] = value
            writeDatabase(data)
            return Result.success(Unit)
        }

        val rootValue = data[rootKey]?.let {
            if (it !is JsonObject) {
                JsonObject(mutableMapOf())
            } else {
                it
            }
        } ?: JsonObject(mutableMapOf())

        val result = setNestedValue(rootValue, key.drop(1), value)
        data[rootKey] = result
        writeDatabase(data)
        return Result.success(Unit)
    }

    override fun delete(key: List<String>): Result<Unit> {
        if (key.isEmpty()) {
            return Result.failure(IllegalStateException("Key cannot be empty"))
        }

        val data = readDatabase().toMutableMap()
        val rootKey = key.first()
        val rootValue = data[rootKey]

        if (rootValue == null) {
            return Result.failure(IllegalStateException("No such key"))
        }

        if (key.size == 1) {
            data.remove(rootKey)
            writeDatabase(data)
            return Result.success(Unit)
        }

        if (rootValue !is JsonObject) {
            return Result.failure(IllegalStateException("Cannot delete from non-object value"))
        }

        val result = deleteNestedValue(rootValue, key.drop(1))
        return result.map { newRootValue ->
            data[rootKey] = newRootValue
            writeDatabase(data)
        }
    }

    private fun getNestedValue(element: JsonElement, path: List<String>): Result<JsonElement> {
        var current = element
        for (key in path) {
            if (current !is JsonObject) {
                return Result.failure(IllegalStateException("Cannot navigate through non-object value"))
            }
            current = current[key] ?: return Result.failure(IllegalStateException("No such key"))
        }
        return Result.success(current)
    }

    private fun setNestedValue(element: JsonElement, path: List<String>, value: JsonElement): JsonElement {
        if (path.isEmpty()) {
            return value
        }

        val currentKey = path.first()
        val remainingPath = path.drop(1)

        val jsonObject = (element as? JsonObject)?.toMutableMap() ?: mutableMapOf()
        val currentValue = jsonObject[currentKey]

        jsonObject[currentKey] = if (remainingPath.isEmpty()) {
            value
        } else {
            val nestedValue = currentValue?.let {
                if (it !is JsonObject) {
                    JsonObject(mutableMapOf())
                } else {
                    it
                }
            } ?: JsonObject(mutableMapOf())
            setNestedValue(nestedValue, remainingPath, value)
        }

        return JsonObject(jsonObject)
    }

    private fun deleteNestedValue(element: JsonElement, path: List<String>): Result<JsonElement> {
        if (element !is JsonObject) {
            return Result.failure(IllegalStateException("Cannot delete from non-object value"))
        }

        val currentKey = path.first()
        val remainingPath = path.drop(1)

        if (!element.containsKey(currentKey)) {
            return Result.failure(IllegalStateException("No such key"))
        }

        val jsonObject = element.toMutableMap()

        if (remainingPath.isEmpty()) {
            jsonObject.remove(currentKey)
            return Result.success(JsonObject(jsonObject))
        }

        val currentValue = jsonObject[currentKey]
        if (currentValue !is JsonObject) {
            return Result.failure(IllegalStateException("Cannot delete from non-object value"))
        }

        return deleteNestedValue(currentValue, remainingPath).map { newValue ->
            jsonObject[currentKey] = newValue
            JsonObject(jsonObject)
        }
    }
}
