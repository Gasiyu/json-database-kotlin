package jsondatabase.database

import kotlinx.serialization.json.*

class InMemoryDatabase : Database {
    private val storage = mutableMapOf<String, JsonElement>()

    override fun get(key: List<String>): Result<JsonElement> {
        if (key.isEmpty()) {
            return Result.failure(IllegalStateException("Key cannot be empty"))
        }

        val rootKey = key.first()
        val rootValue = storage[rootKey]

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

        val rootKey = key.first()

        if (key.size == 1) {
            storage[rootKey] = value
            return Result.success(Unit)
        }

        val rootValue = storage[rootKey]?.let {
            if (it !is JsonObject) {
                JsonObject(mutableMapOf())
            } else {
                it
            }
        } ?: JsonObject(mutableMapOf())

        val result = setNestedValue(rootValue, key.drop(1), value)
        storage[rootKey] = result
        return Result.success(Unit)
    }

    override fun delete(key: List<String>): Result<Unit> {
        if (key.isEmpty()) {
            return Result.failure(IllegalStateException("Key cannot be empty"))
        }

        val rootKey = key.first()
        val rootValue = storage[rootKey]

        if (rootValue == null) {
            return Result.failure(IllegalStateException("No such key"))
        }

        if (key.size == 1) {
            storage.remove(rootKey)
            return Result.success(Unit)
        }

        if (rootValue !is JsonObject) {
            return Result.failure(IllegalStateException("Cannot delete from non-object value"))
        }

        return deleteNestedValue(rootValue, key.drop(1)).map { newRootValue ->
            storage[rootKey] = newRootValue
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
